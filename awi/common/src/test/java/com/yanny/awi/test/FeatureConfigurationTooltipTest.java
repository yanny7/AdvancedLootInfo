package com.yanny.awi.test;

import com.yanny.awi.plugin.server.FeatureConfigurationTooltipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.awi.test.utils.TestUtils.assertTooltip;

public class FeatureConfigurationTooltipTest {
    private static final Holder<PlacedFeature> PLACED_FEATURE = Holder.direct(new PlacedFeature(
            Holder.direct(new ConfiguredFeature<>(Feature.NO_OP, NoneFeatureConfiguration.INSTANCE)), List.of()
    ));

    @Test
    public void testCountConfiguration() {
        assertTooltip(FeatureConfigurationTooltipUtils.getCountConfigurationTooltip(UTILS, new CountConfiguration(5)).build(), List.of(
                "Count:",
                "  -> Count: 5"
        ));
        assertTooltip(FeatureConfigurationTooltipUtils.getCountConfigurationTooltip(UTILS, new CountConfiguration(UniformInt.of(1, 2))).build(), List.of(
                "Count:",
                "  -> Count: 1-2 (Uniform)"
        ));
    }

    @Test
    public void testOreConfiguration() {
        assertTooltip(FeatureConfigurationTooltipUtils.getOreConfigurationTooltip(UTILS, new OreConfiguration(Collections.emptyList(), 5, 0.5f)).build(), List.of(
                "Ore:",
                "  -> Discard Chance On Air Exposure: 0.5",
                "  -> Size: 5"
        ));
        assertTooltip(FeatureConfigurationTooltipUtils.getOreConfigurationTooltip(UTILS, new OreConfiguration(AlwaysTrueTest.INSTANCE, Blocks.FURNACE.defaultBlockState(), 2, 0.25f)).build(), List.of(
                "Ore:",
                "  -> Discard Chance On Air Exposure: 0.25",
                "  -> Size: 2",
                "  -> Target States:",
                "    -> State:",
                "      -> Block: Furnace",
                "      -> Properties:",
                "        -> facing: north",
                "        -> lit: false",
                "    -> Target:",
                "      -> Always True"
        ));
    }

    @Test
    public void testBlockColumnConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getBlockColumnConfigurationTooltip(UTILS, new BlockColumnConfiguration(
                List.of(BlockColumnConfiguration.layer(ConstantInt.of(3), BlockStateProvider.simple(Blocks.STONE))),
                Direction.UP,
                BlockPredicate.ONLY_IN_AIR_PREDICATE,
                true
        )).build(), List.of(
                "Block Column:",
                "  -> Layers:",
                "    -> Entry:",
                "      -> Height: 3",
                "      -> State:",
                "        -> Auto-detected: minecraft:simple_state_provider",
                "          -> state:",
                "            -> minecraft:stone",
                "  -> Direction: UP",
                "  -> Allowed Placement:",
                "    -> Matching Blocks:",
                "      -> Block: Air",
                "      -> Offset: [0,0,0]",
                "  -> Prioritize Tip: true"
        ));
    }

    @Test
    public void testBlockPileConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getBlockPileConfigurationTooltip(UTILS, new BlockPileConfiguration(BlockStateProvider.simple(Blocks.SAND))).build(), List.of(
                "Block Pile:",
                "  -> StateProvider:",
                "    -> Auto-detected: minecraft:simple_state_provider",
                "      -> state:",
                "        -> minecraft:sand"
        ));
    }

    @Test
    public void testBlockStateConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getBlockStateConfigurationTooltip(UTILS, new BlockStateConfiguration(Blocks.STONE.defaultBlockState())).build(), List.of(
                "Block State:",
                "  -> State:",
                "    -> Block: Stone"
        ));
    }

    @Test
    public void testColumnFeatureConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getColumnFeatureConfigurationTooltip(UTILS, new ColumnFeatureConfiguration(ConstantInt.of(1), ConstantInt.of(4))).build(), List.of(
                "Column Feature:",
                "  -> Reach: 1",
                "  -> Height: 4"
        ));
    }

    @Test
    public void testDeltaFeatureConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getDeltaFeatureConfigurationTooltip(UTILS, new DeltaFeatureConfiguration(
                Blocks.MAGMA_BLOCK.defaultBlockState(),
                Blocks.OBSIDIAN.defaultBlockState(),
                ConstantInt.of(3),
                ConstantInt.of(1)
        )).build(), List.of(
                "Delta Feature:",
                "  -> Contents:",
                "    -> Block: Magma Block",
                "  -> Rim:",
                "    -> Block: Obsidian",
                "  -> Size: 3",
                "  -> Rim Size: 1"
        ));
    }

    @Test
    public void testDiscConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getDiscConfigurationTooltip(UTILS, new DiskConfiguration(
                RuleBasedBlockStateProvider.simple(Blocks.SAND),
                BlockPredicate.solid(),
                ConstantInt.of(3),
                2
        )).build(), List.of(
                "Disk:",
                "  -> StateProvider:",
                "    -> Not implemented: [net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider]",
                "  -> Target:",
                "    -> Solid:",
                "      -> Offset: [0,0,0]",
                "  -> Radius: 3",
                "  -> Half Height: 2"
        ));
    }

    @Test
    public void testDripstoneClusterConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getDripstoneClusterConfigurationTooltip(UTILS, new DripstoneClusterConfiguration(
                10,
                ConstantInt.of(6),
                ConstantInt.of(3),
                1,
                2,
                ConstantInt.of(4),
                ConstantFloat.of(0.7f),
                ConstantFloat.of(0.5f),
                0.2f,
                3,
                4
        )).build(), List.of(
                "Dripstone Cluster:",
                "  -> Search Range: 10",
                "  -> Height: 6",
                "  -> Radius: 3",
                "  -> Max Height Diff: 1",
                "  -> Height Deviation: 2",
                "  -> Layer Thickness: 4",
                "  -> Density: %s",
                "    -> Auto-detected: minecraft:constant",
                "      -> 0.7",
                "  -> Wetness: %s",
                "    -> Auto-detected: minecraft:constant",
                "      -> 0.5",
                "  -> Edge Chance: 0.2",
                "  -> Chance Radius: 3",
                "  -> Height Bias Radius: 4"
        ));
    }

    @Test
    public void testEndGatewayConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getEndGatewayConfigurationTooltip(UTILS, EndGatewayConfiguration.knownExit(new BlockPos(1, 2, 3), true)).build(), List.of(
                "End Gateway:",
                "  -> Exit: [1,2,3]",
                "  -> Exact: true"
        ));
        assertTooltip(FeatureConfigurationTooltipUtils.getEndGatewayConfigurationTooltip(UTILS, EndGatewayConfiguration.delayedExitSearch()).build(), List.of(
                "End Gateway:",
                "  -> Exact: false"
        ));
    }

    @Test
    public void testGeodeConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getGeodeConfigurationTooltip(UTILS, new GeodeConfiguration(
                new GeodeBlockSettings(
                        BlockStateProvider.simple(Blocks.AIR),
                        BlockStateProvider.simple(Blocks.AMETHYST_BLOCK),
                        BlockStateProvider.simple(Blocks.BUDDING_AMETHYST),
                        BlockStateProvider.simple(Blocks.CALCITE),
                        BlockStateProvider.simple(Blocks.SMOOTH_BASALT),
                        List.of(Blocks.AMETHYST_CLUSTER.defaultBlockState()),
                        BlockTags.WOOL,
                        BlockTags.LOGS
                ),
                new GeodeLayerSettings(1.7, 2.2, 3.2, 4.2),
                new GeodeCrackSettings(1.0, 2.0, 2),
                0.35,
                0.0,
                true,
                ConstantInt.of(5),
                ConstantInt.of(4),
                ConstantInt.of(2),
                -16,
                16,
                0.05,
                1
        )).build(), List.of(
                "Geode:",
                "  -> Geode Block Settings:",
                "    -> Filling Provider:",
                "      -> Auto-detected: minecraft:simple_state_provider",
                "        -> state:",
                "          -> minecraft:air",
                "    -> Inner Layer Provider:",
                "      -> Auto-detected: minecraft:simple_state_provider",
                "        -> state:",
                "          -> minecraft:amethyst_block",
                "    -> Alternate Inner Layer Provider:",
                "      -> Auto-detected: minecraft:simple_state_provider",
                "        -> state:",
                "          -> minecraft:budding_amethyst",
                "    -> Middle Layer Provider:",
                "      -> Auto-detected: minecraft:simple_state_provider",
                "        -> state:",
                "          -> minecraft:calcite",
                "    -> Outer Layer Provider:",
                "      -> Auto-detected: minecraft:simple_state_provider",
                "        -> state:",
                "          -> minecraft:smooth_basalt",
                "    -> Inner Placements:",
                "      -> Block: Amethyst Cluster",
                "      -> Properties:",
                "        -> facing: up",
                "        -> waterlogged: false",
                "    -> Cannot Replace:",
                "    -> Invalid Blocks:",
                "  -> Geode Layer Settings:",
                "    -> Filling: 1.7",
                "    -> Inner Layer: 2.2",
                "    -> Middle Layer: 3.2",
                "    -> Outer Layer: 4.2",
                "  -> Geode Crack Settings:",
                "    -> Generate Crack Chance: 1.0",
                "    -> Base Crack Size: 2.0",
                "    -> Crack Point Offset: 2",
                "  -> Potential Placement Chance: 0.35",
                "  -> Alternate Layer Chance: 0.0",
                "  -> Require Alternate Layer: true",
                "  -> Outer Wall Distance: 5",
                "  -> Distribution Points: 4",
                "  -> Point Offset: 2",
                "  -> Min Gen Offset: -16",
                "  -> Max Gen Offset: 16",
                "  -> Noise Multiplier: 0.05",
                "  -> Invalid Blocks Threshold: 1"
        ));
    }

    @Test
    public void testHugeMushroomFeatureConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getHugeMushroomFeatureConfigurationTooltip(UTILS, new HugeMushroomFeatureConfiguration(
                BlockStateProvider.simple(Blocks.RED_MUSHROOM_BLOCK),
                BlockStateProvider.simple(Blocks.MUSHROOM_STEM),
                2
        )).build(), List.of(
                "Huge Mushroom Feature:",
                "  -> Cap Provider:",
                "    -> Auto-detected: minecraft:simple_state_provider",
                "      -> state:",
                "        -> down:",
                "          -> true",
                "          -> true",
                "          -> true",
                "          -> true",
                "          -> true",
                "          -> true",
                "        -> minecraft:red_mushroom_block",
                "  -> Stem Provider:",
                "    -> Auto-detected: minecraft:simple_state_provider",
                "      -> state:",
                "        -> down:",
                "          -> true",
                "          -> true",
                "          -> true",
                "          -> true",
                "          -> true",
                "          -> true",
                "        -> minecraft:mushroom_stem",
                "  -> Foliage Radius: 2"
        ));
    }

    @Test
    public void testLargeDripstoneConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getLargeDripstoneConfigurationTooltip(UTILS, new LargeDripstoneConfiguration(
                30,
                ConstantInt.of(6),
                ConstantFloat.of(4.0f),
                0.4f,
                ConstantFloat.of(1.0f),
                ConstantFloat.of(1.0f),
                ConstantFloat.of(0.6f),
                8,
                1.0f
        )).build(), List.of(
                "Large Dripstone:",
                "  -> Search Range: 30",
                "  -> Column Radius: 6",
                "  -> Height Scale: %s",
                "    -> Auto-detected: minecraft:constant",
                "      -> 4.0",
                "  -> Radius To Height Ratio: 0.4",
                "  -> Stalactite Bluntness: %s",
                "    -> Auto-detected: minecraft:constant",
                "      -> 1.0",
                "  -> Stalagmite Bluntness: %s",
                "    -> Auto-detected: minecraft:constant",
                "      -> 1.0",
                "  -> Wind Speed: %s",
                "    -> Auto-detected: minecraft:constant",
                "      -> 0.6",
                "  -> Min Radius For Wind: 8",
                "  -> Min Bluntness For Wind: 1.0"
        ));
    }

    @Test
    public void testLayeredConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getLayeredConfigurationTooltip(UTILS, new LayerConfiguration(3, Blocks.DIRT.defaultBlockState())).build(), List.of(
                "Layered:",
                "  -> Height: 3",
                "  -> State:",
                "    -> Block: Dirt"
        ));
    }

    @Test
    public void testMultifaceGrowthConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getMultifaceGrowthConfigurationTooltip(UTILS, new MultifaceGrowthConfiguration(
                (MultifaceBlock) Blocks.GLOW_LICHEN,
                8,
                true,
                false,
                true,
                0.3f,
                HolderSet.direct(Holder.direct(Blocks.STONE))
        )).build(), List.of(
                "Multiface Growth:",
                "  -> Place Block: Glow Lichen",
                "  -> Search Range: 8",
                "  -> Can Place On Floor: true",
                "  -> Can Place On Ceiling: false",
                "  -> Can Place On Wall: true",
                "  -> Chance Of Spreading: 0.3",
                "  -> Can Be Placed On:",
                "    -> Stone"
        ));
    }

    @Test
    public void testNetherForestVegetationConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getNetherForestVegetationConfigurationTooltip(UTILS, new NetherForestVegetationConfig(
                BlockStateProvider.simple(Blocks.CRIMSON_ROOTS),
                2,
                3
        )).build(), List.of(
                "Nether Forest Vegetation:",
                "  -> Spread Width: 2",
                "  -> Spread Height: 3"
        ));
    }

    @Test
    public void testNoneFeatureConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getNoneFeatureConfigurationTooltip(UTILS, NoneFeatureConfiguration.INSTANCE).build(), List.of(
                "None Feature:"
        ));
    }

    @Test
    public void testPointedDripstoneConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getPointedDripstoneConfigurationTooltip(UTILS, new PointedDripstoneConfiguration(0.2f, 0.7f, 0.5f, 0.5f)).build(), List.of(
                "Pointed Dripstone:",
                "  -> Chance Of Taller Dripstone: 0.2",
                "  -> Chance Of Directional Speed: 0.7",
                "  -> Chance Of Spread Radius 2: 0.5",
                "  -> Chance Of Spread Radius 3: 0.5"
        ));
    }

    @Test
    public void testProbabilityFeatureConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getProbabilityFeatureConfigurationTooltip(UTILS, new ProbabilityFeatureConfiguration(0.3f)).build(), List.of(
                "Probability Feature:",
                "  -> Probability: 0.3"
        ));
    }

    @Test
    public void testRandomBooleanFeatureConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getRandomBooleanFeatureConfigurationTooltip(UTILS, new RandomBooleanFeatureConfiguration(PLACED_FEATURE, PLACED_FEATURE)).build(), List.of(
                "Random Boolean Feature:",
                "  -> Feature True:",
                "    -> Not implemented: [net.minecraft.world.level.levelgen.placement.PlacedFeature]",
                "  -> Feature False:",
                "    -> Not implemented: [net.minecraft.world.level.levelgen.placement.PlacedFeature]"
        ));
    }

    @Test
    public void testRandomFeatureConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getRandomFeatureConfigurationTooltip(UTILS, new RandomFeatureConfiguration(
                List.of(new WeightedPlacedFeature(PLACED_FEATURE, 0.5f)),
                PLACED_FEATURE
        )).build(), List.of(
                "Random Feature:",
                "  -> Features:",
                "    -> Feature:",
                "      -> Not implemented: [net.minecraft.world.level.levelgen.placement.PlacedFeature]",
                "    -> Chance: 0.5",
                "  -> Default Feature:",
                "    -> Not implemented: [net.minecraft.world.level.levelgen.placement.PlacedFeature]"
        ));
    }

    @Test
    public void testRandomPatchConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getRandomPatchConfigurationTooltip(UTILS, new RandomPatchConfiguration(64, 5, 2, PLACED_FEATURE)).build(), List.of(
                "Random Patch:",
                "  -> Tries: 64",
                "  -> XZ Spread: 5",
                "  -> Y Spread: 2",
                "  -> Feature:",
                "    -> Not implemented: [net.minecraft.world.level.levelgen.placement.PlacedFeature]"
        ));
    }

    @Test
    public void testReplaceableBlockConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getReplaceableBlockConfigurationTooltip(UTILS, new ReplaceBlockConfiguration(Blocks.STONE.defaultBlockState(), Blocks.DIRT.defaultBlockState())).build(), List.of(
                "Replaceable Block:",
                "  -> Target States:",
                "    -> State:",
                "      -> Block: Dirt",
                "    -> Target:",
                "      -> Auto-detected: minecraft:blockstate_match",
                "        -> block_state:",
                "          -> minecraft:stone"
        ));
    }

    @Test
    public void testReplaceableSphereConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getReplaceableSphereConfigurationTooltip(UTILS, new ReplaceSphereConfiguration(
                Blocks.STONE.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                ConstantInt.of(3)
        )).build(), List.of(
                "Replaceable Sphere:",
                "  -> Target State:",
                "    -> Block: Stone",
                "  -> Replace State:",
                "    -> Block: Dirt",
                "  -> Radius: 3"
        ));
    }

    @Test
    public void testRootSystemConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getRootSystemConfigurationTooltip(UTILS, new RootSystemConfiguration(
                PLACED_FEATURE,
                3,
                2,
                BlockTags.WOOL,
                BlockStateProvider.simple(Blocks.DIRT),
                20,
                32,
                2,
                4,
                BlockStateProvider.simple(Blocks.DIRT),
                20,
                2,
                BlockPredicate.solid()
        )).build(), List.of(
                "Root System:",
                "  -> Tree Feature:",
                "    -> Not implemented: [net.minecraft.world.level.levelgen.placement.PlacedFeature]",
                "  -> Required Vertical Space For Tree: 3",
                "  -> Root Radius: 2",
                "  -> Root Replaceable: minecraft:wool",
                "  -> Root State Provider:",
                "    -> Auto-detected: minecraft:simple_state_provider",
                "      -> state:",
                "        -> minecraft:dirt",
                "  -> Root Placement Attempts: 20",
                "  -> Root Column Max Height: 32",
                "  -> Hanging Root Radius: 2",
                "  -> Hanging Root Vertical Span: 4",
                "  -> Hanging Root State Provider:",
                "    -> Auto-detected: minecraft:simple_state_provider",
                "      -> state:",
                "        -> minecraft:dirt",
                "  -> Hanging Root Placement Attempts: 20",
                "  -> Allowed Vertical Water For Tree: 2",
                "  -> Allowed Tree Position:",
                "    -> Solid:",
                "      -> Offset: [0,0,0]"
        ));
    }

    @Test
    public void testSculkPatchConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getSculkPatchConfigurationTooltip(UTILS, new SculkPatchConfiguration(
                2, 5, 3, 1, 1, ConstantInt.of(2), 0.5f
        )).build(), List.of(
                "Sculk Patch:",
                "  -> Charge Count: 2",
                "  -> Amount Per Charge: 5",
                "  -> Spread Attempts: 3",
                "  -> Growth Rounds: 1",
                "  -> Spread Rounds: 1",
                "  -> Extra Rare Growths: 2",
                "  -> Catalyst Chance: 0.5"
        ));
    }

    @Test
    public void testSimpleBlockConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getSimpleBlockConfigurationTooltip(UTILS, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.STONE))).build(), List.of(
                "Simple Block:",
                "  -> To Place:",
                "    -> Auto-detected: minecraft:simple_state_provider",
                "      -> state:",
                "        -> minecraft:stone"
        ));
    }

    @Test
    public void testSimpleRandomFeatureConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getSimpleRandomFeatureConfigurationTooltip(UTILS, new SimpleRandomFeatureConfiguration(HolderSet.direct(PLACED_FEATURE))).build(), List.of(
                "Simple Random Features:",
                "  -> Features:",
                "    -> Not implemented: [net.minecraft.world.level.levelgen.placement.PlacedFeature]"
        ));
    }

    @Test
    public void testSpikeConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getSpikeConfigurationTooltip(UTILS, new SpikeConfiguration(
                false,
                List.of(new SpikeFeature.EndSpike(1, 2, 3, 4, true)),
                new BlockPos(5, 6, 7)
        )).build(), List.of(
                "Spike:",
                "  -> Is Crystal Vulnerable: false",
                "  -> Spikes:",
                "    -> Center X: 1",
                "    -> Center Z: 2",
                "    -> Radius: 3",
                "    -> Height: 4",
                "    -> Is Guarded: true",
                "  -> Crystal Beam Target: [5,6,7]"
        ));
    }

    @Test
    public void testSpringConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getSpringConfigurationTooltip(UTILS, new SpringConfiguration(
                Fluids.WATER.defaultFluidState(),
                true,
                4,
                1,
                HolderSet.direct(Holder.direct(Blocks.STONE))
        )).build(), List.of(
                "Spring:",
                "  -> State:",
                "    -> Not implemented: [net.minecraft.world.level.material.FluidState]",
                "  -> Requires Block Below: true",
                "  -> Rock Count: 4",
                "  -> Hole Count: 1",
                "  -> Valid Blocks:",
                "    -> Stone"
        ));
    }

    @Test
    public void testTreeConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getTreeConfigurationTooltip(UTILS, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.OAK_LOG),
                new StraightTrunkPlacer(5, 2, 0),
                BlockStateProvider.simple(Blocks.OAK_LEAVES),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                new TwoLayersFeatureSize(1, 0, 1)
        ).build()).build(), List.of(
                "Tree:",
                "  -> Trunk Provider:",
                "    -> Auto-detected: minecraft:simple_state_provider",
                "      -> state:",
                "        -> axis:",
                "          -> y",
                "        -> minecraft:oak_log",
                "  -> Dirt Provider:",
                "    -> Auto-detected: minecraft:simple_state_provider",
                "      -> state:",
                "        -> minecraft:dirt",
                "  -> Trunk Placer:",
                "    -> Auto-detected: minecraft:straight_trunk_placer",
                "      -> base_height: 5",
                "      -> height_rand_a: 2",
                "      -> height_rand_b: 0",
                "  -> Foliage Provider:",
                "    -> Auto-detected: minecraft:simple_state_provider",
                "      -> state:",
                "        -> distance:",
                "          -> false",
                "          -> false",
                "          -> 7",
                "        -> minecraft:oak_leaves",
                "  -> Foliage Placer:",
                "    -> Auto-detected: minecraft:blob_foliage_placer",
                "      -> radius: 2",
                "      -> offset: 0",
                "      -> height: 3",
                "  -> Minimum Size:",
                "    -> Auto-detected: minecraft:two_layers_feature_size",
                "      -> limit: 1",
                "      -> lower_size: 0",
                "      -> upper_size: 1",
                "  -> Ignore Vines: false",
                "  -> Force Dirt: false"
        ));
    }

    @Test
    public void testTwistingVinesConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getTwistingVinesConfigurationTooltip(UTILS, new TwistingVinesConfig(4, 8, 12)).build(), List.of(
                "Twisting Vines:",
                "  -> Spread Width: 4",
                "  -> Spread Height: 8",
                "  -> Max Height: 12"
        ));
    }

    @Test
    public void testUnderwaterMagmaConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getUnderwaterMagmaConfigurationTooltip(UTILS, new UnderwaterMagmaConfiguration(5, 2, 0.5f)).build(), List.of(
                "Underwater Magma:",
                "  -> Floor Range Search: 5",
                "  -> Placement Radius Around Floor: 2",
                "  -> Probability Per Position: 0.5"
        ));
    }

    @Test
    public void testVegetationPatchConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getVegetationPatchConfigurationTooltip(UTILS, new VegetationPatchConfiguration(
                BlockTags.WOOL,
                BlockStateProvider.simple(Blocks.PODZOL),
                PLACED_FEATURE,
                CaveSurface.FLOOR,
                ConstantInt.of(3),
                0.5f,
                5,
                0.3f,
                ConstantInt.of(2),
                0.1f
        )).build(), List.of(
                "Vegetation Patch:",
                "  -> Replaceable: minecraft:wool",
                "  -> Ground State:",
                "    -> Auto-detected: minecraft:simple_state_provider",
                "      -> state:",
                "        -> snowy:",
                "          -> false",
                "        -> minecraft:podzol",
                "  -> Vegetation Feature:",
                "    -> Not implemented: [net.minecraft.world.level.levelgen.placement.PlacedFeature]",
                "  -> Surface: FLOOR",
                "  -> Depth: 3",
                "  -> Extra Bottom Block Chance: 0.5",
                "  -> Vertical Range: 5",
                "  -> Vegetation Chance: 0.3",
                "  -> XZ Radius: 2",
                "  -> Extra Edge Column Chance: 0.1"
        ));
    }
}
