package com.yanny.awi.test;

import com.yanny.aci.api.RangeValue;
import com.yanny.awi.api.BlockInfo;
import com.yanny.awi.plugin.common.nodes.NodeUtils;
import com.yanny.awi.test.utils.BaseLayoutTestUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BaseLayoutBandsTest {
    private static final List<Long> SEEDS = List.of(1234L, 987654321L, -42L);
    private static final NodeUtils.ScanSettings DENSE = new NodeUtils.ScanSettings(32, 1, 16, 24, 120, 8, 32, true);
    private static List<ResourceKey<Biome>> BIOMES;
    private static Set<Block> BAND_ONLY;

    @BeforeAll
    static void setUp() {
        BaseLayoutTestUtils.bootstrap();
        BIOMES = List.of(Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS);
        BAND_ONLY = Set.of(Blocks.DYED_TERRACOTTA.yellow(), Blocks.DYED_TERRACOTTA.brown(), Blocks.DYED_TERRACOTTA.red(), Blocks.DYED_TERRACOTTA.lightGray());
    }

    @Test
    public void testBandsFollowThePalette() {
        List<Executable> checks = new ArrayList<>();

        for (long seed : SEEDS) {
            BlockState[] palette = randomState(seed).surfaceSystem().clayBands;

            for (ResourceKey<Biome> biome : BIOMES) {
                String name = "seed=%d %s".formatted(seed, biome.identifier());
                Set<BlockInfo> infos = scan(seed, biome, NodeUtils.ScanSettings.DEFAULT);
                Map<Block, NavigableSet<Integer>> bands = bands(infos);
                NavigableSet<Integer> window = window(infos);

                checks.add(() -> assertFalse(window.isEmpty(), name + " reports no band filler"));
                checks.add(() -> assertTrue(bands.values().stream().allMatch(window::containsAll), name + " band outside the filler"));

                for (Block block : BAND_ONLY) {
                    Set<Integer> expected = new TreeSet<>();

                    window.stream().filter((y) -> palette[Math.floorMod(y, palette.length)].is(block)).forEach(expected::add);
                    checks.add(() -> assertEquals(expected, bands.getOrDefault(block, new TreeSet<>()), name + " " + block));
                    checks.add(() -> assertTrue(infos.stream().noneMatch((info) -> info.block() == block && info.layerShift() == 0),
                            name + " " + block + " is still measured"));
                }
            }
        }

        assertAll(checks);
    }

    @Test
    @EnabledIfSystemProperty(named = "awi.baselayout.bands", matches = "true")
    public void testDenseSamplingFindsTheSameWindowBounds() {
        List<Executable> checks = new ArrayList<>();

        for (long seed : SEEDS) {
            for (ResourceKey<Biome> biome : BIOMES) {
                String name = "seed=%d %s".formatted(seed, biome.identifier());
                NavigableSet<Integer> fast = window(scan(seed, biome, NodeUtils.ScanSettings.DEFAULT));
                NavigableSet<Integer> dense = window(scan(seed, biome, DENSE));

                System.out.printf("%s window default %s..%s, dense %s..%s%n", name, fast.first(), fast.last(), dense.first(), dense.last());
                checks.add(() -> assertEquals(List.of(dense.first(), dense.last()), List.of(fast.first(), fast.last()), name));
            }
        }

        assertAll(checks);
    }

    private static Set<BlockInfo> scan(long seed, ResourceKey<Biome> biomeKey, NodeUtils.ScanSettings settings) {
        NodeUtils.DimensionContext context = new NodeUtils.DimensionContext(BaseLayoutTestUtils.lookup(), generator(), randomState(seed), BaseLayoutTestUtils.SURFACE_RULE_HANDLERS);
        Holder<Biome> biome = BaseLayoutTestUtils.lookup().lookupOrThrow(Registries.BIOME).getOrThrow(biomeKey);

        return NodeUtils.getBaseBlocksForBiome(context, biome, new NodeUtils.ScanOptions(settings, false)).getBlockInfos();
    }

    private static Map<Block, NavigableSet<Integer>> bands(Set<BlockInfo> infos) {
        Map<Block, NavigableSet<Integer>> bands = new HashMap<>();

        for (BlockInfo info : infos) {
            if (info.layerShift() > 0) {
                NavigableSet<Integer> levels = bands.computeIfAbsent(info.block(), (k) -> new TreeSet<>());

                for (RangeValue range : info.ranges()) {
                    IntStream.rangeClosed((int) range.min(), (int) range.max()).forEach(levels::add);
                }
            }
        }

        return bands;
    }

    private static NavigableSet<Integer> window(Set<BlockInfo> infos) {
        List<BlockInfo> filler = infos.stream()
                .filter((info) -> info.block() == Blocks.TERRACOTTA && info.storageType() == BlockInfo.StorageType.ABSOLUTE)
                .toList();
        NavigableSet<Integer> window = new TreeSet<>();

        assertEquals(1, filler.size(), "exactly one filler entry");
        assertEquals(1, filler.get(0).ranges().size(), "the filler is one range");
        assertEquals(0, filler.get(0).layerShift(), "the filler has no shift");
        IntStream.rangeClosed((int) filler.get(0).ranges().get(0).min(), (int) filler.get(0).ranges().get(0).max()).forEach(window::add);

        return window;
    }

    private static NoiseBasedChunkGenerator generator() {
        return (NoiseBasedChunkGenerator) BaseLayoutTestUtils.levelStems().getValueOrThrow(LevelStem.OVERWORLD).generator();
    }

    private static RandomState randomState(long seed) {
        return RandomState.create(BaseLayoutTestUtils.registryAccess().lookupOrThrow(Registries.NOISE), seed,
                generator().generatorSettings().value());
    }
}
