package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

final class ModJarIndex {
    private static final Map<String, Optional<ModJarIndex>> INDEXES = new ConcurrentHashMap<>();
    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";

    private final List<byte[]> classes;
    private final ClassNode[] parsed;
    private final ClassLoader loader;

    private ModJarIndex(List<byte[]> classes, ClassLoader loader) {
        this.classes = classes;
        this.parsed = new ClassNode[classes.size()];
        this.loader = loader;
    }

    @Nullable
    static ModJarIndex of(Class<?> anyClass) {
        CodeSource source = anyClass.getProtectionDomain().getCodeSource();
        URL location = source != null ? source.getLocation() : null;

        if (location == null || anyClass.getClassLoader() == null) {
            return null;
        }

        return INDEXES.computeIfAbsent(location.toString(), (key) -> Optional.ofNullable(read(location, anyClass.getClassLoader()))).orElse(null);
    }

    static void clearCaches() {
        INDEXES.clear();
    }

    @NotNull
    List<Caller> callers(CallSites.Sink sink) {
        return find(sink.method().name(), (call) -> sink.matches(call, loader), sink.argument());
    }

    @NotNull
    List<Caller> callersOf(String owner, String name, String desc, int param) {
        return find(name, (call) -> call.owner.equals(owner) && call.name.equals(name) && call.desc.equals(desc), param);
    }

    @NotNull
    private List<Caller> find(String name, Predicate<MethodInsnNode> matches, int argument) {
        byte[] pattern = name.getBytes(StandardCharsets.UTF_8);
        List<Caller> callers = new ArrayList<>();

        for (int i = 0; i < classes.size(); i++) {
            if (indexOf(classes.get(i), pattern) < 0) {
                continue;
            }

            ClassNode node = parse(i);

            if (node == null) {
                continue;
            }

            Boolean entity = null;

            for (MethodNode method : node.methods) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn instanceof MethodInsnNode call && matches.test(call)) {
                        if (entity == null) {
                            entity = isEntity(node);
                        }

                        callers.add(new Caller(node.name, entity, method, CallSites.argument(node.name, method, call, argument, loader)));
                    }
                }
            }
        }

        return callers;
    }

    @Nullable
    private ClassNode parse(int index) {
        if (parsed[index] == null) {
            try {
                ClassNode node = new ClassNode();

                new ClassReader(classes.get(index)).accept(node, ClassReader.SKIP_FRAMES);
                parsed[index] = node;
            } catch (Throwable e) {
                return null;
            }
        }

        return parsed[index];
    }

    private boolean isEntity(ClassNode node) {
        if (Stream.of(node.invisibleAnnotations, node.visibleAnnotations).filter(Objects::nonNull).flatMap(List::stream).anyMatch((a) -> a.desc.equals(MIXIN))) {
            return false;
        }

        try {
            return Entity.class.isAssignableFrom(Resolution.load(node.name, loader));
        } catch (Fail fail) {
            return false;
        }
    }

    @Nullable
    private static ModJarIndex read(URL location, ClassLoader loader) {
        try {
            Path path = Paths.get(location.toURI());

            if (Files.isDirectory(path)) {
                return new ModJarIndex(read(path), loader);
            }

            try (FileSystem jar = FileSystems.newFileSystem(path)) {
                return new ModJarIndex(read(jar.getPath("/")), loader);
            }
        } catch (Throwable e) {
            return null;
        }
    }

    @NotNull
    private static List<byte[]> read(Path root) throws Exception {
        List<byte[]> classes = new ArrayList<>();

        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : (Iterable<Path>) files.filter((p) -> p.toString().endsWith(".class"))::iterator) {
                classes.add(Files.readAllBytes(file));
            }
        }

        return classes;
    }

    private static int indexOf(byte[] data, byte[] pattern) {
        outer:
        for (int i = 0; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }

            return i;
        }

        return -1;
    }

    record Caller(String owner, boolean entity, MethodNode method, CallSites.Source source) {
    }
}
