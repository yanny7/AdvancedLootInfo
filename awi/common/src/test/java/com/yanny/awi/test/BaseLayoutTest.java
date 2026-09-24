package com.yanny.awi.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.yanny.awi.plugin.common.nodes.BandlandsLayout;
import com.yanny.awi.plugin.common.nodes.NodeUtils;
import com.yanny.awi.plugin.common.nodes.SurfaceRuleSpecializer;
import com.yanny.awi.test.utils.BaseLayoutTestUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression guard for the surface-rule scan ({@link NodeUtils#getBaseBlocksForBiome}): scans every vanilla biome of
 * every vanilla dimension over several seeds and compares the discovered base blocks against a committed golden file.
 * Several seeds, because the scan's coverage is seed-dependent — noise-gated blocks are only hit for some seeds.
 * <p>
 * Regenerate after an intentional change with:
 * {@code ./gradlew :awi:common:test --tests "com.yanny.awi.test.BaseLayoutTest" -Dawi.baselayout.regenerate=true}
 * <p>
 * The golden file alone cannot guard {@link com.yanny.awi.plugin.common.nodes.SurfaceRuleSpecializer}: it is a pure
 * cost cut, so a specializer that silently stops specializing keeps producing the same file. The second test compares
 * the two paths against each other instead.
 */
public class BaseLayoutTest {
    private static final List<Long> SEEDS = List.of(1234L, 987654321L, -42L);
    private static final String GOLDEN_RESOURCE = "base_layout.json";
    private static final Path GOLDEN_FILE = Path.of("src/test/resources", GOLDEN_RESOURCE);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String BIOME_CONDITION = "minecraft:biome";
    private static final String NOISE_THRESHOLD = "minecraft:noise_threshold";
    private static final String VERTICAL_GRADIENT = "minecraft:vertical_gradient";
    private static final String HOLE = "minecraft:hole";

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

    /**
     * Per-biome rule specialization only removes branches that cannot fire for that biome, so it must never change what
     * the scan finds.
     */
    @Test
    public void testSpecializationDoesNotChangeResult() {
        NodeUtils.ScanSettings settings = NodeUtils.ScanSettings.DEFAULT;
        NodeUtils.ScanSettings unspecialized = new NodeUtils.ScanSettings(settings.columnsPerRound(), settings.surfaceHeightStep(),
                settings.stableRounds(), settings.extentStableRounds(), settings.maxRounds(), settings.maxCeilingThickness(),
                settings.deepWalkWindow(), false);

        Map<String, String> specializedEntries = flatten(BaseLayoutTestUtils.scan(SEEDS.getFirst(), settings));
        Map<String, String> unspecializedEntries = flatten(BaseLayoutTestUtils.scan(SEEDS.getFirst(), unspecialized));
        Set<String> keys = new TreeSet<>(unspecializedEntries.keySet());

        keys.addAll(specializedEntries.keySet());

        assertAll(keys.stream().map((key) -> () ->
                assertEquals(unspecializedEntries.get(key), specializedEntries.get(key), key)));
    }

    @Test
    public void testSpecializationPrunesEveryBiomeTest() {
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, BaseLayoutTestUtils.lookup());
        List<Executable> checks = new ArrayList<>();
        List<String> specializedDimensions = new ArrayList<>();

        for (LevelStem levelStem : BaseLayoutTestUtils.levelStems()) {
            if (!(levelStem.generator() instanceof NoiseBasedChunkGenerator generator)) {
                continue;
            }

            SurfaceRules.RuleSource rule = generator.generatorSettings().value().surfaceRule();
            JsonElement original = encode(ops, rule);

            if (countNodes(original, BIOME_CONDITION) == 0) {
                continue;
            }

            SurfaceRuleSpecializer specializer = new SurfaceRuleSpecializer(rule, BaseLayoutTestUtils.lookup(), Map.of(), false);
            int originalNodes = countNodes(original, null);

            specializedDimensions.add(String.valueOf(BaseLayoutTestUtils.levelStems().getKey(levelStem)));

            for (Holder<Biome> biome : generator.getBiomeSource().possibleBiomes()) {
                JsonElement specialized = encode(ops, specializer.specialize(biome));
                String name = BaseLayoutTestUtils.biomeName(biome);

                checks.add(() -> assertEquals(0, countNodes(specialized, BIOME_CONDITION), name + " still tests biomes"));
                checks.add(() -> assertTrue(countNodes(specialized, null) < originalNodes, name + " was not pruned"));
            }
        }

        assertEquals(List.of("minecraft:overworld", "minecraft:the_nether"), specializedDimensions.stream().sorted().toList());
        assertAll(checks);
    }

    @Test
    public void testBandlandsReplacedByGhosts() {
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, BaseLayoutTestUtils.lookup());
        NoiseBasedChunkGenerator generator = (NoiseBasedChunkGenerator) BaseLayoutTestUtils.levelStems().getValueOrThrow(LevelStem.OVERWORLD).generator();
        SurfaceRules.RuleSource rule = generator.generatorSettings().value().surfaceRule();
        RandomState randomState = RandomState.create(generator.generatorSettings().value(),
                BaseLayoutTestUtils.registryAccess().lookupOrThrow(Registries.NOISE), SEEDS.get(0));
        SurfaceRuleSpecializer specializer = new SurfaceRuleSpecializer(rule, BaseLayoutTestUtils.lookup(),
                Map.of(BandlandsLayout.TYPE.toString(), BandlandsLayout.create(randomState)), false);
        List<Executable> checks = new ArrayList<>();

        assertTrue(countNodes(encode(ops, rule), BandlandsLayout.TYPE.toString()) > 1);
        assertEquals(1, specializer.ghosts().size(), "identical bandlands nodes must share one ghost");
        assertEquals(0, countNodes(encode(ops, specializer.baseRule()), BandlandsLayout.TYPE.toString()));

        for (Holder<Biome> biome : generator.getBiomeSource().possibleBiomes()) {
            String specialized = encode(ops, specializer.specialize(biome)).toString();
            boolean hasGhost = specialized.contains("minecraft:light");
            boolean badlands = biome.is(Biomes.BADLANDS) || biome.is(Biomes.ERODED_BADLANDS) || biome.is(Biomes.WOODED_BADLANDS);

            checks.add(() -> assertEquals(badlands, hasGhost, BaseLayoutTestUtils.biomeName(biome)));
        }

        assertAll(checks);
    }

    @Test
    public void testNoiseGatesReplacedByCoins() {
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, BaseLayoutTestUtils.lookup());
        List<Executable> checks = new ArrayList<>();

        for (LevelStem levelStem : BaseLayoutTestUtils.levelStems()) {
            if (!(levelStem.generator() instanceof NoiseBasedChunkGenerator generator)) {
                continue;
            }

            String name = String.valueOf(BaseLayoutTestUtils.levelStems().getKey(levelStem));
            SurfaceRules.RuleSource rule = generator.generatorSettings().value().surfaceRule();
            JsonElement original = encode(ops, rule);
            JsonElement base = encode(ops, new SurfaceRuleSpecializer(rule, BaseLayoutTestUtils.lookup(), Map.of(), false).baseRule());

            checks.add(() -> assertEquals(0, countNodes(base, NOISE_THRESHOLD) + countNodes(base, HOLE), name));
            checks.add(() -> assertEquals(countNodes(original, VERTICAL_GRADIENT) + countNodes(original, NOISE_THRESHOLD)
                    + countNodes(original, HOLE), countNodes(base, VERTICAL_GRADIENT), name));
        }

        assertAll(checks);
    }

    @Test
    public void testSameBlocksInEverySeed() throws IOException {
        Map<String, Set<String>> blocksPerBiome = new TreeMap<>();
        List<Executable> checks = new ArrayList<>();

        readGolden().entrySet().forEach((seed) -> seed.getValue().getAsJsonObject().entrySet().forEach((dimension) ->
                dimension.getValue().getAsJsonObject().entrySet().forEach((biome) -> {
                    Set<String> blocks = new TreeSet<>();

                    biome.getValue().getAsJsonArray().forEach((line) -> blocks.add(line.getAsString().split(" ", 2)[0]));

                    String key = "%s %s".formatted(dimension.getKey(), biome.getKey());
                    Set<String> previous = blocksPerBiome.putIfAbsent(key, blocks);

                    if (previous != null) {
                        checks.add(() -> assertEquals(previous, blocks, key + " seed=" + seed.getKey()));
                    }
                })));

        assertAll(checks);
    }

    private static JsonElement encode(DynamicOps<JsonElement> ops, SurfaceRules.RuleSource rule) {
        return SurfaceRules.RuleSource.CODEC.encodeStart(ops, rule).getOrThrow();
    }

    private static int countNodes(JsonElement element, @Nullable String type) {
        int count = 0;

        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                count += countNodes(child, type);
            }
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();

            if (object.has("type") && (type == null || type.equals(object.get("type").getAsString()))) {
                count++;
            }

            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                count += countNodes(entry.getValue(), type);
            }
        }

        return count;
    }

    /** Flattens {@code dimension -> biome -> blocks} into one comparable entry per biome, for precise failures. */
    private static Map<String, String> flatten(Map<String, Map<String, List<String>>> scan) {
        Map<String, String> entries = new LinkedHashMap<>();

        scan.forEach((dimension, biomes) -> biomes.forEach((biome, blocks) ->
                entries.put("%s %s".formatted(dimension, biome), blocks.toString())));

        return entries;
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
