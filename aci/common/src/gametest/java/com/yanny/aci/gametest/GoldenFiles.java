package com.yanny.aci.gametest;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public record GoldenFiles(Path goldenDir, Path actualDir, boolean regenerate) {
    private static final String REMOVED_FILE = "removed.txt";

    @NotNull
    public static GoldenFiles fromProperties(String modId) {
        return new GoldenFiles(Path.of(System.getProperty(modId + ".golden.dir")), Path.of(System.getProperty(modId + ".golden.actual")),
                Boolean.getBoolean(modId + ".golden.regenerate"));
    }

    @NotNull
    public GoldenFiles resolve(String variant) {
        return new GoldenFiles(goldenDir.resolve(variant), actualDir.resolve(variant), regenerate);
    }

    @NotNull
    public static Map<String, String> diff(Map<String, String> base, Map<String, String> actual) {
        Map<String, String> files = new TreeMap<>();
        String removed = base.keySet().stream().filter((key) -> !actual.containsKey(key)).map((key) -> key + "\n").collect(Collectors.joining());

        actual.forEach((key, value) -> {
            if (!value.equals(base.get(key))) {
                files.put(key, value);
            }
        });

        if (!removed.isEmpty()) {
            files.put(REMOVED_FILE, removed);
        }

        return files;
    }

    @NotNull
    public Map<String, String> expected() {
        if (!Files.isDirectory(goldenDir)) {
            throw new IllegalStateException("no golden files in " + goldenDir);
        }

        return read(goldenDir);
    }

    @NotNull
    public List<String> compare(Map<String, String> actual) {
        write(regenerate ? goldenDir : actualDir, actual);

        if (regenerate) {
            return List.of();
        }

        if (!Files.isDirectory(goldenDir)) {
            return List.of("no golden files in " + goldenDir);
        }

        Map<String, String> expected = read(goldenDir);
        List<String> differences = new ArrayList<>();

        expected.keySet().stream().filter((key) -> !actual.containsKey(key)).forEach((key) -> differences.add("missing " + key));
        actual.keySet().stream().filter((key) -> !expected.containsKey(key)).forEach((key) -> differences.add("new " + key));
        actual.forEach((key, value) -> {
            if (expected.containsKey(key) && !expected.get(key).equals(value)) {
                differences.add("changed " + key);
            }
        });

        return differences;
    }

    private static void write(Path dir, Map<String, String> files) {
        try {
            if (Files.isDirectory(dir)) {
                try (var paths = Files.walk(dir)) {
                    for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                        Files.delete(path);
                    }
                }
            }

            Files.createDirectories(dir);

            for (Map.Entry<String, String> entry : files.entrySet()) {
                Path path = dir.resolve(entry.getKey());

                Files.createDirectories(path.getParent());
                Files.writeString(path, entry.getValue());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @NotNull
    private static Map<String, String> read(Path dir) {
        Map<String, String> files = new TreeMap<>();

        try (var paths = Files.walk(dir)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                files.put(dir.relativize(path).toString().replace('\\', '/'), Files.readString(path));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return files;
    }
}
