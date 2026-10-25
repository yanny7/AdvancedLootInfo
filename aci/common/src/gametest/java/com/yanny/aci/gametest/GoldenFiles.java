package com.yanny.aci.gametest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record GoldenFiles(Path goldenDir, Path actualDir, boolean regenerate) {
    private static final String REMOVED_FILE = "removed.txt";
    private static final String UNSTABLE_FILE = "unstable.txt";
    private static final String UNSTABLE = "<unstable>";

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
    public List<String> compare(Map<String, String> output) {
        List<String> differences = new ArrayList<>();
        Map<String, String> actual = mask(output, unstable(), differences);

        write(regenerate ? goldenDir : actualDir, actual);

        if (regenerate) {
            return differences;
        }

        Map<String, String> expected = Files.isDirectory(goldenDir) ? read(goldenDir) : Map.of();

        expected.keySet().stream().filter((key) -> !actual.containsKey(key)).forEach((key) -> differences.add("missing " + key));
        actual.keySet().stream().filter((key) -> !expected.containsKey(key)).forEach((key) -> differences.add("new " + key));
        actual.forEach((key, value) -> {
            if (expected.containsKey(key) && !expected.get(key).equals(value)) {
                differences.add("changed " + key);
            }
        });

        return differences;
    }

    @Nullable
    private String unstable() {
        Path path = goldenDir.resolve(UNSTABLE_FILE);

        try {
            return Files.isRegularFile(path) ? Files.readString(path) : null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @NotNull
    private static Map<String, String> mask(Map<String, String> output, @Nullable String unstable, List<String> differences) {
        if (unstable == null) {
            return output;
        }

        Map<String, String> files = new TreeMap<>(output);

        unstable.lines().filter((line) -> !line.isBlank() && !line.startsWith("#")).forEach((regex) -> {
            Pattern pattern = Pattern.compile(regex);
            boolean[] used = {false};

            files.replaceAll((key, value) -> {
                Matcher matcher = pattern.matcher(value);

                if (!matcher.find()) {
                    return value;
                }

                used[0] = true;
                return matcher.replaceAll(UNSTABLE);
            });

            if (!used[0]) {
                differences.add("unused mask " + regex);
            }
        });

        files.put(UNSTABLE_FILE, unstable);
        return files;
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
