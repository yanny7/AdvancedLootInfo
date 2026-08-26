package com.yanny.awi.test;

import com.yanny.awi.plugin.server.PlacementModifierTooltipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;

public class PlacementModifierTooltipTest {
    @Test
    public void testBiomeFilterTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getBiomeFilterTooltip(UTILS, BiomeFilter.biome()).build(), List.of());
    }

    @Test
    public void testBlockPredicateFilterTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getBlockPredicateFilterTooltip(UTILS, BlockPredicateFilter.forPredicate(BlockPredicate.solid())).build(), List.of(
                "Block Predicate Filter:",
                "  -> Solid:"
        ));
    }

    @Test
    public void testCountOnEveryLayerPlacementTooltip() {
        //noinspection deprecation
        assertTooltip(PlacementModifierTooltipUtils.getCountOnEveryLayerPlacementTooltip(UTILS, CountOnEveryLayerPlacement.of(5)).build(), List.of(
                "Count On Every Layer:",
                "  -> Count:",
                "    -> Constant:",
                "      -> Value: 5"
        ));
        //noinspection deprecation
        assertTooltip(PlacementModifierTooltipUtils.getCountOnEveryLayerPlacementTooltip(UTILS, CountOnEveryLayerPlacement.of(UniformInt.of(1, 3))).build(), List.of(
                "Count On Every Layer:",
                "  -> Count:",
                "    -> Uniform:",
                "      -> Range: 1-3"
        ));
    }

    @Test
    public void testCountPlacementTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getCountPlacementTooltip(UTILS, CountPlacement.of(3)).build(), List.of(
                "Count Placement:",
                "  -> Count:",
                "    -> Constant:",
                "      -> Value: 3"
        ));
        assertTooltip(PlacementModifierTooltipUtils.getCountPlacementTooltip(UTILS, CountPlacement.of(UniformInt.of(2, 6))).build(), List.of(
                "Count Placement:",
                "  -> Count:",
                "    -> Uniform:",
                "      -> Range: 2-6"
        ));
    }

    @Test
    public void testEnvironmentScanPlacementTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getEnvironmentScanPlacementTooltip(UTILS, EnvironmentScanPlacement.scanningFor(
                Direction.UP,
                BlockPredicate.solid(),
                BlockPredicate.solid(),
                32
        )).build(), List.of(
                "Environment Scan Placement:",
                "  -> Direction Of Search: Up",
                "  -> Target Condition:",
                "    -> Solid:",
                "  -> Allowed Search Condition:",
                "    -> Solid:",
                "  -> Max Steps: 32"
        ));
    }

    @Test
    public void testHeightmapPlacementTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getHeightmapPlacementTooltip(UTILS, HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING)).build(), List.of(
                "Heightmap Placement:",
                "  -> Heightmap: Ground or Water Surface"
        ));
    }

    @Test
    public void testHeightRangePlacementTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getHeightRangePlacementTooltip(UTILS, HeightRangePlacement.of(ConstantHeight.of(VerticalAnchor.absolute(5)))).build(), List.of(
                "Height Range Placement:",
                "  -> Constant:",
                "    -> Absolute Y: 5"
        ));
        assertTooltip(PlacementModifierTooltipUtils.getHeightRangePlacementTooltip(UTILS, HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(10)))).build(), List.of(
                "Height Range Placement:",
                "  -> Uniform:",
                "    -> Min:",
                "      -> Absolute Y: 0",
                "    -> Max:",
                "      -> Absolute Y: 10"
        ));
    }

    @Test
    public void testInSquarePlacementTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getInSquarePlacementTooltip(UTILS, InSquarePlacement.spread()).build(), List.of());
    }

    @Test
    public void testNoiseBasedCountPlacementTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getNoiseBasedCountPlacementTooltip(UTILS, NoiseBasedCountPlacement.of(5, 1.5, 0.2)).build(), List.of(
                "Noise Based Count Placement:",
                "  -> Noise To Count Ratio: 5",
                "  -> Noise Factor: 1.5",
                "  -> Noise Offset: 0.2"
        ));
    }

    @Test
    public void testNoiseThresholdCountPlacementTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getNoiseThresholdCountPlacementTooltip(UTILS, NoiseThresholdCountPlacement.of(0.5, 2, 8)).build(), List.of(
                "Noise Threshold Count Placement:",
                "  -> Noise Level: 0.5",
                "  -> Below Noise: 2",
                "  -> Above Noise: 8"
        ));
    }

    @Test
    public void testRarityFilterTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getRarityFilterTooltip(UTILS, RarityFilter.onAverageOnceEvery(20)).build(), List.of(
                "Rarity Filter:",
                "  -> Chance: 20"
        ));
    }

    @Test
    public void testRandomOffsetPlacementTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getRandomOffsetPlacementTooltip(UTILS, RandomOffsetPlacement.of(ConstantInt.of(1), ConstantInt.of(2))).build(), List.of(
                "Random Offset:",
                "  -> XZ Spread:",
                "    -> Constant:",
                "      -> Value: 1",
                "  -> Y Spread:",
                "    -> Constant:",
                "      -> Value: 2"
        ));
    }

    @Test
    public void testSurfaceRelativeThresholdFilterTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getSurfaceRelativeThresholdFilterTooltip(UTILS, SurfaceRelativeThresholdFilter.of(Heightmap.Types.WORLD_SURFACE_WG, Integer.MIN_VALUE, 5)).build(), List.of(
                "Surface Relative Threshold Filter:",
                "  -> Heightmap: Highest Block, Plants Included",
                "  -> Range: ≤5"
        ));
        assertTooltip(PlacementModifierTooltipUtils.getSurfaceRelativeThresholdFilterTooltip(UTILS, SurfaceRelativeThresholdFilter.of(Heightmap.Types.WORLD_SURFACE_WG, 3, Integer.MAX_VALUE)).build(), List.of(
                "Surface Relative Threshold Filter:",
                "  -> Heightmap: Highest Block, Plants Included",
                "  -> Range: ≥3"
        ));
        assertTooltip(PlacementModifierTooltipUtils.getSurfaceRelativeThresholdFilterTooltip(UTILS, SurfaceRelativeThresholdFilter.of(Heightmap.Types.WORLD_SURFACE_WG, 2, 8)).build(), List.of(
                "Surface Relative Threshold Filter:",
                "  -> Heightmap: Highest Block, Plants Included",
                "  -> Range: 2-8"
        ));
    }

    @Test
    public void testSurfaceWaterDepthFilterTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getSurfaceWaterDepthFilterTooltip(UTILS, SurfaceWaterDepthFilter.forMaxDepth(3)).build(), List.of(
                "Surface Water Depth Filter:",
                "  -> Max Water Depth: 3"
        ));
    }

    @Test
    public void testFixedPlacementTooltip() {
        assertTooltip(PlacementModifierTooltipUtils.getFixedPlacementTooltip(UTILS, FixedPlacement.of(new BlockPos(1, 2, 3))).build(), List.of(
                "Fixed Placement:",
                "  -> Position: [1,2,3]"
        ));
        assertTooltip(PlacementModifierTooltipUtils.getFixedPlacementTooltip(UTILS, FixedPlacement.of(
                new BlockPos(1, 2, 3),
                new BlockPos(3, 2, 1)
        )).build(), List.of(
                "Fixed Placement:",
                "  -> Positions:",
                "    -> [1,2,3]",
                "    -> [3,2,1]"
        ));
    }
}
