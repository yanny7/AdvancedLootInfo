package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class ModJarIndex {
    private static final Map<String, Optional<ModJarIndex>> INDEXES = new ConcurrentHashMap<>();
    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final String UNION_SEPARATOR = "%23";
    private static final int REACHING_NAMES = 64;

    private final List<byte[]> classes;
    private final ClassNode[] parsed;
    private final Map<CallSites.Sink, List<Caller>> sinkCallers = new HashMap<>();
    private final Map<CallSites.Sink, Optional<Set<String>>> sinkReaching = new HashMap<>();
    private final ClassLoader loader;

    private ModJarIndex(List<byte[]> classes, ClassLoader loader) {
        this.classes = classes;
        this.parsed = new ClassNode[classes.size()];
        this.loader = loader;
    }

    @Nullable
    static ModJarIndex of(Class<?> anyClass) {
        URL location = location(anyClass);

        if (location == null) {
            return null;
        }

        return INDEXES.computeIfAbsent(location.toString(), (key) -> Optional.ofNullable(read(location, anyClass.getClassLoader()))).orElse(null);
    }

    @Nullable
    static URL location(Class<?> anyClass) {
        CodeSource source = anyClass.getClassLoader() != null ? anyClass.getProtectionDomain().getCodeSource() : null;

        return source != null ? source.getLocation() : null;
    }

    static void clearCaches() {
        INDEXES.clear();
    }

    @NotNull
    List<Caller> callers(CallSites.Sink sink) {
        return sinkCallers.computeIfAbsent(sink, (s) -> find(s.marker().name(), (call) -> s.matches(call, loader), (owner, method, call) -> s.source(owner, method, call, loader)));
    }

    @Nullable
    Set<String> reaching(CallSites.Sink sink, int depth) {
        return sinkReaching.computeIfAbsent(sink, (s) -> Optional.ofNullable(computeReaching(s, depth))).orElse(null);
    }

    @NotNull
    List<Caller> callersOf(String owner, String name, String desc, int param) {
        return find(name, (call) -> call.owner.equals(owner) && call.name.equals(name) && call.desc.equals(desc),
                (caller, method, call) -> CallSites.argument(caller, method, call, param, loader));
    }

    @NotNull
    private List<Caller> find(String name, Predicate<MethodInsnNode> matches, ArgumentSource argument) {
        byte[] pattern = name.getBytes(StandardCharsets.UTF_8);
        int[] shift = shift(pattern);
        List<Caller> callers = new ArrayList<>();

        for (int i = 0; i < classes.size(); i++) {
            if (indexOf(classes.get(i), pattern, shift) < 0) {
                continue;
            }

            ClassNode node = parse(i);

            if (node == null) {
                continue;
            }

            Boolean entity = null;

            for (MethodNode method : node.methods) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (!(insn instanceof MethodInsnNode call) || !matches.test(call)) {
                        continue;
                    }

                    CallSites.Source source = argument.of(node.name, method, call);

                    if (source != null) {
                        if (entity == null) {
                            entity = isEntity(node);
                        }

                        callers.add(new Caller(node.name, entity, method, source));
                    }
                }
            }
        }

        return callers;
    }

    @Nullable
    private Set<String> computeReaching(CallSites.Sink sink, int depth) {
        Set<String> level = new HashSet<>();

        for (Caller caller : callers(sink)) {
            level.add(caller.method().name + caller.method().desc);
        }

        Set<String> reaching = new HashSet<>(level);

        for (int d = 0; d < depth && !level.isEmpty(); d++) {
            if (level.size() > REACHING_NAMES) {
                return null;
            }

            List<byte[]> patterns = level.stream().map((key) -> key.substring(0, key.indexOf('('))).distinct().map((n) -> n.getBytes(StandardCharsets.UTF_8)).toList();
            List<int[]> shifts = patterns.stream().map(ModJarIndex::shift).toList();
            Set<String> next = new HashSet<>();

            for (int i = 0; i < classes.size(); i++) {
                byte[] data = classes.get(i);

                if (IntStream.range(0, patterns.size()).noneMatch((p) -> indexOf(data, patterns.get(p), shifts.get(p)) >= 0)) {
                    continue;
                }

                ClassNode node = parse(i);

                if (node == null) {
                    continue;
                }

                for (MethodNode method : node.methods) {
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn instanceof MethodInsnNode call && level.contains(call.name + call.desc)) {
                            next.add(method.name + method.desc);
                            break;
                        }
                    }
                }
            }

            next.removeAll(reaching);
            reaching.addAll(next);
            level = next;
        }

        return reaching;
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
            URI uri = location.toURI();
            Path jar = jarFile(uri);

            if (jar != null) {
                return new ModJarIndex(readZip(jar), loader);
            }

            Path path = Paths.get(uri);

            if (Files.isDirectory(path)) {
                return new ModJarIndex(readTree(path), loader);
            }

            try (FileSystem zip = FileSystems.newFileSystem(path)) {
                return new ModJarIndex(readTree(zip.getPath("/")), loader);
            }
        } catch (Throwable e) {
            return null;
        }
    }

    @Nullable
    private static Path jarFile(URI uri) {
        String part = uri.getRawSchemeSpecificPart();

        if (!"file".equals(uri.getScheme())) {
            int end = part.indexOf(UNION_SEPARATOR);

            part = end >= 0 ? part.substring(0, end) : part;
        }

        try {
            Path path = Paths.get(URI.create("file:" + part));

            return Files.isRegularFile(path) ? path : null;
        } catch (Throwable e) {
            return null;
        }
    }

    @NotNull
    private static List<byte[]> readZip(Path jar) throws Exception {
        List<byte[]> classes = new ArrayList<>();

        try (ZipFile zip = new ZipFile(jar.toFile())) {
            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();

                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    try (InputStream stream = zip.getInputStream(entry)) {
                        classes.add(stream.readAllBytes());
                    }
                }
            }
        }

        return classes;
    }

    @NotNull
    private static List<byte[]> readTree(Path root) throws Exception {
        List<byte[]> classes = new ArrayList<>();

        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : (Iterable<Path>) files.filter((p) -> p.toString().endsWith(".class"))::iterator) {
                classes.add(Files.readAllBytes(file));
            }
        }

        return classes;
    }

    private static int[] shift(byte[] pattern) {
        int[] shift = new int[256];

        Arrays.fill(shift, pattern.length);

        for (int i = 0; i < pattern.length - 1; i++) {
            shift[pattern[i] & 0xFF] = pattern.length - 1 - i;
        }

        return shift;
    }

    private static int indexOf(byte[] data, byte[] pattern, int[] shift) {
        int last = pattern.length - 1;

        for (int i = 0; i <= data.length - pattern.length; i += shift[data[i + last] & 0xFF]) {
            int j = last;

            while (j >= 0 && data[i + j] == pattern[j]) {
                j--;
            }

            if (j < 0) {
                return i;
            }
        }

        return -1;
    }

    record Caller(String owner, boolean entity, MethodNode method, CallSites.Source source) {
    }

    private interface ArgumentSource {
        @Nullable
        CallSites.Source of(String owner, MethodNode method, MethodInsnNode call);
    }
}
