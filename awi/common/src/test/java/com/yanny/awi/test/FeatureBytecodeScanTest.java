package com.yanny.awi.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yanny.awi.plugin.server.FeatureBytecodeScanner;
import com.yanny.awi.test.utils.BaseLayoutTestUtils;
import com.yanny.awi.test.utils.FeatureInstances;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression guard for {@link FeatureBytecodeScanner}: scans every vanilla {@link Feature} and compares the discovered
 * blocks against a committed golden file.
 * <p>
 * The classes to scan come from {@link FeatureInstances}, not from the {@code worldgen/feature} data registry:
 * {@code FEATURE_TYPE} holds codecs and names no class, and vanilla data instantiates only 47 of the 58 types, which
 * would silently drop {@code sculk_patch} and the corals from the guard.
 * <p>
 * Alongside the blocks it records how much of the scanner's {@code MAX_METHODS} budget each feature consumed. That is
 * the number to watch when widening the walk: the walk is breadth-first and hard-capped, so a wider walk can push a
 * feature over the cap and make it lose blocks it finds today. A pure improvement must add blocks and remove none.
 * <p>
 * Tags are recorded as tag keys rather than as their members, matching what the scanner reports, so the file does not
 * depend on any tags being bound.
 * <p>
 * Regenerate after an intentional change with:
 * {@code ./gradlew :awi:common:test --tests "com.yanny.awi.test.FeatureBytecodeScanTest" -Dawi.featurescan.regenerate=true}
 */
public class FeatureBytecodeScanTest {
    private static final String GOLDEN_RESOURCE = "feature_bytecode_scan.json";
    private static final Path GOLDEN_FILE = Path.of("src/test/resources", GOLDEN_RESOURCE);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @BeforeAll
    static void setUp() {
        BaseLayoutTestUtils.bootstrap();
    }

    @Test
    public void testFeatureScanMatchesGolden() throws IOException {
        // The scanner caches per feature class across the whole JVM.
        FeatureBytecodeScanner.clearCaches();

        Map<String, Map<String, Object>> actual = new TreeMap<>();
        List<String> truncated = new ArrayList<>();
        List<String> conditional = new ArrayList<>();

        for (Map.Entry<String, Class<? extends Feature>> entry : featureClassesByType().entrySet()) {
            String id = entry.getKey();
            FeatureBytecodeScanner.ScanResult result = FeatureBytecodeScanner.scan(entry.getValue());

            Map<String, Object> report = new LinkedHashMap<>();

            report.put("visitedMethods", result.visitedMethods());
            report.put("methodLimitReached", result.methodLimitReached());
            report.put("blocks", blockIds(result.blocks()));
            report.put("configConditionalBlocks", blockIds(result.configConditionalBlocks()));
            report.put("tags", tagIds(result.tags()));
            actual.put(id, report);

            if (!result.configConditionalBlocks().isEmpty()) {
                conditional.add("%s: %s".formatted(id, blockIds(result.configConditionalBlocks())));
            }

            if (result.methodLimitReached()) {
                truncated.add("%s (%s): %d methods, %d blocks".formatted(id, entry.getValue().getSimpleName(),
                        result.visitedMethods(), result.blocks().size()));
            }
        }

        List<String> uncovered = BuiltInRegistries.FEATURE_TYPE.keySet().stream()
                .map(Identifier::toString)
                .filter((id) -> !actual.containsKey(id))
                .sorted()
                .toList();

        System.out.printf("%nfeature bytecode scan: %d features, %d hit the method budget, %d have config-conditional blocks, %d types not instantiated by vanilla data%n",
                actual.size(), truncated.size(), conditional.size(), uncovered.size());
        uncovered.forEach((line) -> System.out.println("  no vanilla instance: " + line));
        truncated.forEach((line) -> System.out.println("  budget reached: " + line));
        conditional.forEach((line) -> System.out.println("  config-conditional: " + line));

        if (Boolean.getBoolean("awi.featurescan.regenerate")) {
            Files.writeString(GOLDEN_FILE, GSON.toJson(actual) + "\n", StandardCharsets.UTF_8);
            System.out.println("Regenerated " + GOLDEN_FILE.toAbsolutePath());
            return;
        }

        Map<String, String> expectedEntries = flatten(readGolden());
        Map<String, String> actualEntries = flatten(GSON.toJsonTree(actual).getAsJsonObject());
        Set<String> keys = new TreeSet<>(expectedEntries.keySet());

        keys.addAll(actualEntries.keySet());

        assertAll(keys.stream().map((key) -> () -> assertEquals(expectedEntries.get(key), actualEntries.get(key), key)));
    }

    /** Feature-type id -> the class implementing it, taken from the one instance per type {@link FeatureInstances} builds. */
    @NotNull
    private static Map<String, Class<? extends Feature>> featureClassesByType() {
        Map<String, Class<? extends Feature>> classes = new TreeMap<>();

        FeatureInstances.features().forEach((id, feature) ->
                classes.put(Identifier.withDefaultNamespace(id).toString(), feature.getClass()));

        return classes;
    }

    /** Flattens {@code feature -> report} into one comparable entry per feature, for precise failures. */
    private static Map<String, String> flatten(JsonObject root) {
        Map<String, String> entries = new LinkedHashMap<>();

        root.entrySet().forEach((feature) -> entries.put(feature.getKey(), feature.getValue().toString()));

        return entries;
    }

    private static List<String> blockIds(Set<Block> blocks) {
        return blocks.stream().map(BuiltInRegistries.BLOCK::getKey).map(Identifier::toString).sorted().toList();
    }

    private static List<String> tagIds(Set<TagKey<Block>> tags) {
        return tags.stream().map((tag) -> "#" + tag.location()).sorted().toList();
    }

    private static JsonObject readGolden() throws IOException {
        try (InputStream stream = FeatureBytecodeScanTest.class.getClassLoader().getResourceAsStream(GOLDEN_RESOURCE)) {
            if (stream == null) {
                throw new IOException("Missing golden file " + GOLDEN_FILE + "; regenerate it with -Dawi.featurescan.regenerate=true");
            }

            return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
