package com.yanny.awi.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yanny.awi.plugin.common.nodes.NodeUtils;
import com.yanny.awi.test.utils.BaseLayoutTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression guard for the surface-rule scan ({@link NodeUtils#getBaseBlocksForBiome}): scans every vanilla biome of
 * every vanilla dimension and compares the discovered base blocks against a committed golden file.
 * <p>
 * Several seeds are scanned because the scan's coverage is genuinely seed-dependent — noise-gated blocks (nether
 * soul_sand, frozen-peaks ice) are only hit for some seeds, and badlands band positions differ per seed. A single seed
 * would let a reduction in sampling pass here while silently losing blocks in other worlds.
 * <p>
 * Regenerate after an intentional change with:
 * {@code ./gradlew :awi:common:test --tests "com.yanny.awi.test.BaseLayoutTest" -Dawi.baselayout.regenerate=true}
 */
public class BaseLayoutTest {
    private static final List<Long> SEEDS = List.of(1234L, 987654321L, -42L);
    private static final String GOLDEN_RESOURCE = "base_layout.json";
    private static final Path GOLDEN_FILE = Path.of("src/test/resources", GOLDEN_RESOURCE);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @BeforeAll
    static void setUp() {
        BaseLayoutTestUtils.bootstrap();
    }

    @Test
    public void testBaseLayoutMatchesGolden() throws IOException {
        Map<String, Map<String, Map<String, List<String>>>> actual = new TreeMap<>();

        for (long seed : SEEDS) {
            actual.put(Long.toString(seed), BaseLayoutTestUtils.scan(seed, NodeUtils.ScanSettings.DEFAULT));
        }

        if (Boolean.getBoolean("awi.baselayout.regenerate")) {
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

    /** Flattens {@code seed -> dimension -> biome -> blocks} into one comparable entry per biome, for precise failures. */
    private static Map<String, String> flatten(JsonObject root) {
        Map<String, String> entries = new LinkedHashMap<>();

        root.entrySet().forEach((seed) -> seed.getValue().getAsJsonObject().entrySet().forEach((dimension) ->
                dimension.getValue().getAsJsonObject().entrySet().forEach((biome) ->
                        entries.put("seed=%s %s %s".formatted(seed.getKey(), dimension.getKey(), biome.getKey()),
                                biome.getValue().toString()))));

        return entries;
    }

    private static JsonObject readGolden() throws IOException {
        try (InputStream stream = BaseLayoutTest.class.getClassLoader().getResourceAsStream(GOLDEN_RESOURCE)) {
            if (stream == null) {
                throw new IOException("Missing golden file " + GOLDEN_FILE + "; regenerate it with -Dawi.baselayout.regenerate=true");
            }

            return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
