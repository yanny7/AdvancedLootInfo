package com.yanny.ali.plugin.server.bytecode;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.refmap.IReferenceMapper;
import org.spongepowered.asm.mixin.refmap.ReferenceMapper;
import org.spongepowered.asm.mixin.refmap.RemappingReferenceMapper;
import org.spongepowered.asm.service.MixinService;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class MixinChanges {
    private static final String MIXIN_MERGED = "org.spongepowered.asm.mixin.transformer.meta.MixinMerged";
    private static final String DEFAULT_REFMAP = "mixin.refmap.json";
    private static final Map<Class<?>, List<Target>> CHANGED = new ConcurrentHashMap<>();
    private static final Map<String, IReferenceMapper> REFMAPS = new ConcurrentHashMap<>();

    private MixinChanges() {}

    static void clearCaches() {
        CHANGED.clear();
        REFMAPS.clear();
    }

    static boolean isChanged(Class<?> owner, MethodNode method) {
        return CHANGED.computeIfAbsent(owner, MixinChanges::changed).stream().anyMatch((t) -> t.matches(method.name, method.desc));
    }

    private static List<Target> changed(Class<?> owner) {
        List<Target> changed = new ArrayList<>();
        Set<String> mixinNames = new LinkedHashSet<>();

        try {
            for (Method method : owner.getDeclaredMethods()) {
                for (Annotation annotation : method.getDeclaredAnnotations()) {
                    if (annotation.annotationType().getName().equals(MIXIN_MERGED)) {
                        changed.add(new Target(method.getName(), Type.getMethodDescriptor(method), false));
                        mixinNames.add((String) annotation.annotationType().getMethod("mixin").invoke(annotation));
                    }
                }
            }
        } catch (Throwable ignored) {}

        for (String name : mixinNames) {
            try {
                Set<IMixinInfo> mixins = Mixins.getMixinsForClass(name);

                if (mixins != null) {
                    for (IMixinInfo mixin : mixins) {
                        injected(mixin, owner, changed);
                    }
                }
            } catch (Throwable ignored) {}
        }

        return changed;
    }

    private static void injected(IMixinInfo mixin, Class<?> owner, List<Target> changed) {
        ClassNode node = mixin.getClassNode(ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        ClassNode ownerNode = Interpreter.classNode(owner);
        IReferenceMapper refmap = null;

        for (MethodNode method : node.methods) {
            for (List<AnnotationNode> annotations : Arrays.asList(method.visibleAnnotations, method.invisibleAnnotations)) {
                if (annotations == null) {
                    continue;
                }

                for (AnnotationNode annotation : annotations) {
                    for (String selector : selectors(annotation)) {
                        if (refmap == null) {
                            refmap = REFMAPS.computeIfAbsent(mixin.getConfig().getName(), (name) -> refmap(mixin.getConfig()));
                        }

                        Target target = Target.parse(refmap.remap(mixin.getClassRef(), selector), Type.getInternalName(owner));

                        if (target == null) {
                            continue;
                        }
                        if (ownerNode == null || ownerNode.methods.stream().noneMatch((m) -> target.matches(m.name, m.desc))) {
                            changed.add(Target.ALL);
                        } else {
                            changed.add(target);
                        }
                    }
                }
            }
        }
    }

    private static List<String> selectors(AnnotationNode annotation) {
        List<String> selectors = new ArrayList<>();

        if (annotation.values == null) {
            return selectors;
        }

        for (int i = 0; i + 1 < annotation.values.size(); i += 2) {
            if (annotation.values.get(i).equals("method")) {
                Object value = annotation.values.get(i + 1);

                if (value instanceof String selector) {
                    selectors.add(selector);
                } else if (value instanceof List<?> list) {
                    list.stream().filter(String.class::isInstance).map(String.class::cast).forEach(selectors::add);
                }
            }
        }

        return selectors;
    }

    private static IReferenceMapper refmap(IMixinConfig config) {
        JsonObject json = configJson(config.getName());
        JsonElement refmapName = json != null ? json.get("refmap") : null;
        String name = refmapName != null && refmapName.isJsonPrimitive() ? refmapName.getAsString() : null;

        if (name == null && config.getPlugin() != null) {
            name = config.getPlugin().getRefMapperConfig();
        }

        MixinEnvironment environment = config.getEnvironment();
        IReferenceMapper refmap = ReferenceMapper.read(name != null ? name : DEFAULT_REFMAP);

        if (environment.getOption(MixinEnvironment.Option.REFMAP_REMAP)) {
            refmap = RemappingReferenceMapper.of(environment, refmap);
        }
        if (!refmap.isDefault()) {
            refmap.setContext(environment.getRefmapObfuscationContext());
        }

        return refmap;
    }

    @Nullable
    private static JsonObject configJson(String config) {
        try (InputStream stream = MixinService.getService().getResourceAsStream(config)) {
            return stream != null ? JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject() : null;
        } catch (Throwable e) {
            return null;
        }
    }

    private record Target(String name, @Nullable String desc, boolean prefix) {
        static final Target ALL = new Target("", null, true);

        @Nullable
        static Target parse(String selector, String owner) {
            if (selector.startsWith("/")) {
                return ALL;
            }

            String rest = selector;
            int semicolon = rest.indexOf(';');

            if (rest.startsWith("L") && semicolon > 0 && (rest.indexOf('(') < 0 || semicolon < rest.indexOf('('))) {
                if (!rest.substring(1, semicolon).equals(owner)) {
                    return null;
                }

                rest = rest.substring(semicolon + 1);
            }

            int descStart = rest.indexOf('(');
            String name = descStart >= 0 ? rest.substring(0, descStart) : rest;
            String desc = descStart >= 0 ? rest.substring(descStart) : null;
            int colon = name.indexOf(':');

            if (colon >= 0) {
                name = name.substring(0, colon);
            }

            int wildcard = name.indexOf('*');

            if (wildcard >= 0) {
                return new Target(name.substring(0, wildcard), desc, true);
            }

            return new Target(name, desc, false);
        }

        boolean matches(String methodName, String methodDesc) {
            return (prefix ? methodName.startsWith(name) : methodName.equals(name)) && (desc == null || desc.equals(methodDesc));
        }
    }
}
