package com.yanny.awi.test;

import com.yanny.awi.plugin.server.FeatureConfigurationTooltipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.CocoaDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
                "  -> Count:",
                "    -> Constant:",
                "      -> Value: 5"
        ));
        assertTooltip(FeatureConfigurationTooltipUtils.getCountConfigurationTooltip(UTILS, new CountConfiguration(UniformInt.of(1, 2))).build(), List.of(
                "Count:",
                "  -> Count:",
                "    -> Uniform:",
                "      -> Range: 1-2"
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
                "    -> Height:",
                "      -> Constant:",
                "        -> Value: 3",
                "    -> State:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Stone",
                "  -> Direction: UP",
                "  -> Allowed Placement:",
                "    -> Matching Block Tag:",
                "      -> Tag: minecraft:air",
                "  -> Prioritize Tip: true"
        ));
    }

    @Test
    public void testBlockPileConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getBlockPileConfigurationTooltip(UTILS, new BlockPileConfiguration(BlockStateProvider.simple(Blocks.SAND))).build(), List.of(
                "Block Pile:",
                "  -> State Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Sand"
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
                "  -> Reach:",
                "    -> Constant:",
                "      -> Value: 1",
                "  -> Height:",
                "    -> Constant:",
                "      -> Value: 4"
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
                "  -> Size:",
                "    -> Constant:",
                "      -> Value: 3",
                "  -> Rim Size:",
                "    -> Constant:",
                "      -> Value: 1"
        ));
    }

    @Test
    public void testDiscConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getDiscConfigurationTooltip(UTILS, new DiskConfiguration(
                new RuleBasedBlockStateProvider(BlockStateProvider.simple(Blocks.SAND), List.of(
                        new RuleBasedBlockStateProvider.Rule(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockStateProvider.simple(Blocks.STONE))
                )),
                BlockPredicate.solid(),
                ConstantInt.of(3),
                2
        )).build(), List.of(
                "Disk:",
                "  -> State Provider:",
                "    -> Fallback:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Sand",
                "    -> Rules:",
                "      -> If True:",
                "        -> Matching Blocks:",
                "          -> Block: Air",
                "      -> Then:",
                "        -> Simple:",
                "          -> State:",
                "            -> Block: Stone",
                "  -> Target:",
                "    -> Solid:",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 3",
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
                "  -> Height:",
                "    -> Constant:",
                "      -> Value: 6",
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 3",
                "  -> Max Height Diff: 1",
                "  -> Height Deviation: 2",
                "  -> Layer Thickness:",
                "    -> Constant:",
                "      -> Value: 4",
                "  -> Density:",
                "    -> Constant:",
                "      -> Value: 0.7",
                "  -> Wetness:",
                "    -> Constant:",
                "      -> Value: 0.5",
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
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Air",
                "    -> Inner Layer Provider:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Block of Amethyst",
                "    -> Alternate Inner Layer Provider:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Budding Amethyst",
                "    -> Middle Layer Provider:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Calcite",
                "    -> Outer Layer Provider:",
                "      -> Simple:",
                "        -> State:",
                "          -> Block: Smooth Basalt",
                "    -> Inner Placements:",
                "      -> Block: Amethyst Cluster",
                "      -> Properties:",
                "        -> facing: up",
                "        -> waterlogged: false",
                "    -> Cannot Replace: minecraft:wool",
                "    -> Invalid Blocks: minecraft:logs",
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
                "  -> Outer Wall Distance:",
                "    -> Constant:",
                "      -> Value: 5",
                "  -> Distribution Points:",
                "    -> Constant:",
                "      -> Value: 4",
                "  -> Point Offset:",
                "    -> Constant:",
                "      -> Value: 2",
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
                2,
                BlockPredicate.solid()
        )).build(), List.of(
                "Huge Mushroom Feature:",
                "  -> Cap Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Red Mushroom Block",
                "        -> Properties:",
                "          -> down: true",
                "          -> east: true",
                "          -> north: true",
                "          -> south: true",
                "          -> up: true",
                "          -> west: true",
                "  -> Stem Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Mushroom Stem",
                "        -> Properties:",
                "          -> down: true",
                "          -> east: true",
                "          -> north: true",
                "          -> south: true",
                "          -> up: true",
                "          -> west: true",
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
                "  -> Column Radius:",
                "    -> Constant:",
                "      -> Value: 6",
                "  -> Height Scale:",
                "    -> Constant:",
                "      -> Value: 4.0",
                "  -> Radius To Height Ratio: 0.4",
                "  -> Stalactite Bluntness:",
                "    -> Constant:",
                "      -> Value: 1.0",
                "  -> Stalagmite Bluntness:",
                "    -> Constant:",
                "      -> Value: 1.0",
                "  -> Wind Speed:",
                "    -> Constant:",
                "      -> Value: 0.6",
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
                (MultifaceSpreadeableBlock) Blocks.GLOW_LICHEN,
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
                "  -> Can be Placed On: Stone"
        ));
        assertTooltip(FeatureConfigurationTooltipUtils.getMultifaceGrowthConfigurationTooltip(UTILS, new MultifaceGrowthConfiguration(
                (MultifaceSpreadeableBlock) Blocks.GLOW_LICHEN,
                8,
                true,
                false,
                true,
                0.3f,
                HolderSet.direct(Holder.direct(Blocks.STONE), Holder.direct(Blocks.DIRT))
        )).build(), List.of(
                "Multiface Growth:",
                "  -> Place Block: Glow Lichen",
                "  -> Search Range: 8",
                "  -> Can Place On Floor: true",
                "  -> Can Place On Ceiling: false",
                "  -> Can Place On Wall: true",
                "  -> Chance Of Spreading: 0.3",
                "  -> Can Be Placed On:",
                "    -> Stone",
                "    -> Dirt"
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
                "  -> State Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Crimson Roots",
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
                "  -> Chance Of Directional Spread: 0.7",
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
                "    -> Feature:",
                "      -> Feature: minecraft:no_op",
                "      -> Config:",
                "        -> None Feature:",
                "  -> Feature False:",
                "    -> Feature:",
                "      -> Feature: minecraft:no_op",
                "      -> Config:",
                "        -> None Feature:"
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
                "      -> Feature:",
                "        -> Feature: minecraft:no_op",
                "        -> Config:",
                "          -> None Feature:",
                "    -> Chance: 0.5",
                "  -> Default Feature:",
                "    -> Feature:",
                "      -> Feature: minecraft:no_op",
                "      -> Config:",
                "        -> None Feature:"
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
                "      -> Block State Match:",
                "        -> State:",
                "          -> Block: Stone"
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
                "  -> Radius:",
                "    -> Constant:",
                "      -> Value: 3"
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
                "    -> Feature:",
                "      -> Feature: minecraft:no_op",
                "      -> Config:",
                "        -> None Feature:",
                "  -> Required Vertical Space For Tree: 3",
                "  -> Root Radius: 2",
                "  -> Root Replaceable: minecraft:wool",
                "  -> Root State Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Dirt",
                "  -> Root Placement Attempts: 20",
                "  -> Root Column Max Height: 32",
                "  -> Hanging Root Radius: 2",
                "  -> Hanging Root Vertical Span: 4",
                "  -> Hanging Root State Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Dirt",
                "  -> Hanging Root Placement Attempts: 20",
                "  -> Allowed Vertical Water For Tree: 2",
                "  -> Allowed Tree Position:",
                "    -> Solid:"
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
                "  -> Extra Rare Growths:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Catalyst Chance: 0.5"
        ));
    }

    @Test
    public void testSimpleBlockConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getSimpleBlockConfigurationTooltip(UTILS, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.STONE))).build(), List.of(
                "Simple Block:",
                "  -> Schedule Tick: false",
                "  -> To Place:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Stone"
        ));
    }

    @Test
    public void testSimpleRandomFeatureConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getSimpleRandomFeatureConfigurationTooltip(UTILS, new SimpleRandomFeatureConfiguration(HolderSet.direct(PLACED_FEATURE))).build(), List.of(
                "Simple Random Features:",
                "  -> Features:",
                "    -> Feature:",
                "      -> Feature: minecraft:no_op",
                "      -> Config:",
                "        -> None Feature:"
        ));
    }

    @Test
    public void testSpikeConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getSpikeConfigurationTooltip(UTILS, new SpikeConfiguration(
                Blocks.DIRT.defaultBlockState(),
                BlockPredicate.solid(),
                BlockPredicate.alwaysTrue()
        )).build(), List.of(
                "Spike:",
                "  -> State:",
                "    -> Block: Dirt",
                "  -> Can Place On:",
                "    -> Solid:",
                "  -> Can Replace:",
                "    -> True Block"
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
                "    -> Fluid: minecraft:water",
                "    -> Properties:",
                "      -> falling: true",
                "  -> Requires Block Below: true",
                "  -> Rock Count: 4",
                "  -> Hole Count: 1",
                "  -> Valid Block: Stone"
        ));
        assertTooltip(FeatureConfigurationTooltipUtils.getSpringConfigurationTooltip(UTILS, new SpringConfiguration(
                Fluids.WATER.defaultFluidState(),
                true,
                4,
                1,
                HolderSet.direct(Holder.direct(Blocks.STONE), Holder.direct(Blocks.DIRT))
        )).build(), List.of(
                "Spring:",
                "  -> State:",
                "    -> Fluid: minecraft:water",
                "    -> Properties:",
                "      -> falling: true",
                "  -> Requires Block Below: true",
                "  -> Rock Count: 4",
                "  -> Hole Count: 1",
                "  -> Valid Blocks:",
                "    -> Stone",
                "    -> Dirt"
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
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Oak Log",
                "        -> Properties:",
                "          -> axis: y",
                "  -> Trunk Placer:",
                "    -> Straight Trunk:",
                "      -> Base Height: 5",
                "      -> Height Rand A: 2",
                "      -> Height Rand B: 0",
                "  -> Foliage Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Oak Leaves",
                "        -> Properties:",
                "          -> distance: 7",
                "          -> persistent: false",
                "          -> waterlogged: false",
                "  -> Foliage Placer:",
                "    -> Blob:",
                "      -> Radius:",
                "        -> Constant:",
                "          -> Value: 2",
                "      -> Offset:",
                "        -> Constant:",
                "          -> Value: 0",
                "      -> Height: 3",
                "  -> Minimum Size:",
                "    -> Two Layers:",
                "      -> Limit: 1",
                "      -> Lower Size: 0",
                "      -> Upper Size: 1",
                "  -> Ignore Vines: false",
                "  -> Below Trunk Provider:",
                "    -> Rule Based:",
                "      -> Rules:",
                "        -> If True:",
                "          -> Not:",
                "            -> Predicate:",
                "              -> Matching Block Tag:",
                "                -> Tag: minecraft:cannot_replace_below_tree_trunk",
                "                -> Offset: [0,0,0]",
                "        -> Then:",
                "          -> Simple:",
                "            -> State:",
                "              -> Block: Dirt"
        ));
        assertTooltip(FeatureConfigurationTooltipUtils.getTreeConfigurationTooltip(UTILS, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.OAK_LOG),
                new StraightTrunkPlacer(5, 2, 0),
                BlockStateProvider.simple(Blocks.OAK_LEAVES),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                Optional.of(new MangroveRootPlacer(
                        ConstantInt.of(2),
                        BlockStateProvider.simple(Blocks.MANGROVE_ROOTS),
                        Optional.empty(),
                        new MangroveRootPlacement(
                                HolderSet.direct(Holder.direct(Blocks.MUD)),
                                HolderSet.direct(Holder.direct(Blocks.MUD)),
                                BlockStateProvider.simple(Blocks.MUDDY_MANGROVE_ROOTS),
                                8,
                                15,
                                0.5f
                        )
                )),
                new TwoLayersFeatureSize(1, 0, 1)
        ).decorators(List.of(TrunkVineDecorator.INSTANCE)).build()).build(), List.of(
                "Tree:",
                "  -> Trunk Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Oak Log",
                "        -> Properties:",
                "          -> axis: y",
                "  -> Dirt Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Dirt",
                "  -> Trunk Placer:",
                "    -> Straight Trunk:",
                "      -> Base Height: 5",
                "      -> Height Rand A: 2",
                "      -> Height Rand B: 0",
                "  -> Foliage Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Oak Leaves",
                "        -> Properties:",
                "          -> distance: 7",
                "          -> persistent: false",
                "          -> waterlogged: false",
                "  -> Foliage Placer:",
                "    -> Blob:",
                "      -> Radius:",
                "        -> Constant:",
                "          -> Value: 2",
                "      -> Offset:",
                "        -> Constant:",
                "          -> Value: 0",
                "      -> Height: 3",
                "  -> Root Placer:",
                "    -> Mangrove Root:",
                "      -> Trunk Offset Y:",
                "        -> Constant:",
                "          -> Value: 2",
                "      -> Root Provider:",
                "        -> Simple:",
                "          -> State:",
                "            -> Block: Mangrove Roots",
                "            -> Properties:",
                "              -> waterlogged: false",
                "      -> Mangrove Root Placement:",
                "        -> Can Grow Through: Mud",
                "        -> Muddy Roots In: Mud",
                "        -> Muddy Root Provider:",
                "          -> Simple:",
                "            -> State:",
                "              -> Block: Muddy Mangrove Roots",
                "              -> Properties:",
                "                -> axis: y",
                "        -> Max Root Width: 8",
                "        -> Max Root Length: 15",
                "        -> Random Skew Chance: 0.5",
                "  -> Minimum Size:",
                "    -> Two Layers:",
                "      -> Limit: 1",
                "      -> Lower Size: 0",
                "      -> Upper Size: 1",
                "  -> Decorators:",
                "    -> Trunk Vine",
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
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Podzol",
                "        -> Properties:",
                "          -> snowy: false",
                "  -> Vegetation Feature:",
                "    -> Feature:",
                "      -> Feature: minecraft:no_op",
                "      -> Config:",
                "        -> None Feature:",
                "  -> Surface: FLOOR",
                "  -> Depth:",
                "    -> Constant:",
                "      -> Value: 3",
                "  -> Extra Bottom Block Chance: 0.5",
                "  -> Vertical Range: 5",
                "  -> Vegetation Chance: 0.3",
                "  -> XZ Radius:",
                "    -> Constant:",
                "      -> Value: 2",
                "  -> Extra Edge Column Chance: 0.1"
        ));
    }

    @Test
    public void testLakeConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getLakeConfigurationTooltip(UTILS, new LakeFeature.Configuration(
                BlockStateProvider.simple(Blocks.STONE),
                BlockStateProvider.simple(Blocks.DIRT)
        )).build(), List.of(
                "Lake:",
                "  -> Fluid:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Stone",
                "  -> Barrier:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Dirt"
        ));
    }

    @Test
    public void testFossilFeatureConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getFossilFeatureConfigurationTooltip(UTILS, new FossilFeatureConfiguration(
                List.of(Identifier.fromNamespaceAndPath("minecraft", "fossil/spine_1")),
                List.of(Identifier.fromNamespaceAndPath("minecraft", "fossil_spine_1")),
                Holder.direct(new StructureProcessorList(List.of())),
                Holder.direct(new StructureProcessorList(List.of())),
                3
        )).build(), List.of(
                "Fossil Feature:",
                "  -> Fossil Structure: minecraft:fossil/spine_1",
                "  -> Overlay Structure: minecraft:fossil_spine_1",
                "  -> Max Empty Corners Allowed: 3"
        ));
        assertTooltip(FeatureConfigurationTooltipUtils.getFossilFeatureConfigurationTooltip(UTILS, new FossilFeatureConfiguration(
                List.of(Identifier.fromNamespaceAndPath("minecraft", "fossil/spine_1"), Identifier.fromNamespaceAndPath("minecraft", "fossil/spine_2")),
                List.of(Identifier.fromNamespaceAndPath("minecraft", "fossil_spine_1"), Identifier.fromNamespaceAndPath("minecraft", "fossil_spine_2")),
                Holder.direct(new StructureProcessorList(List.of(BlockIgnoreProcessor.AIR))),
                Holder.direct(new StructureProcessorList(List.of(BlockIgnoreProcessor.AIR))),
                3
        )).build(), List.of(
                "Fossil Feature:",
                "  -> Fossil Structures:",
                "    -> minecraft:fossil/spine_1",
                "    -> minecraft:fossil/spine_2",
                "  -> Overlay Structures:",
                "    -> minecraft:fossil_spine_1",
                "    -> minecraft:fossil_spine_2",
                "  -> Fossil Processors:",
                "    -> Block Ignore:",
                "      -> To Ignore: Air",
                "  -> Overlay Processors:",
                "    -> Block Ignore:",
                "      -> To Ignore: Air",
                "  -> Max Empty Corners Allowed: 3"
        ));
    }

    @Test
    public void testHugeFungusConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getHugeFungusConfigurationTooltip(UTILS, new HugeFungusConfiguration(
                Blocks.NETHERRACK.defaultBlockState(),
                Blocks.CRIMSON_NYLIUM.defaultBlockState(),
                Blocks.SHROOMLIGHT.defaultBlockState(),
                Blocks.WARPED_WART_BLOCK.defaultBlockState(),
                BlockPredicate.solid(),
                true
        )).build(), List.of(
                "Huge Fungus:",
                "  -> Valid Base State:",
                "    -> Block: Netherrack",
                "  -> Stem State:",
                "    -> Block: Crimson Nylium",
                "  -> Hat State:",
                "    -> Block: Shroomlight",
                "  -> Decor State:",
                "    -> Block: Warped Wart Block",
                "  -> Replaceable Blocks:",
                "    -> Solid:",
                "  -> Planted: true"
        ));
    }

    @Test
    public void testFallenTreeConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getFallenTreeConfigurationTooltip(UTILS, new FallenTreeConfiguration.FallenTreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.OAK_LOG),
                ConstantInt.of(5)
        ).stumpDecorators(List.of(new CocoaDecorator(0.2f))).logDecorators(List.of(new CocoaDecorator(0.3f))).build()).build(), List.of(
                "Fallen Tree:",
                "  -> Trunk Provider:",
                "    -> Simple:",
                "      -> State:",
                "        -> Block: Oak Log",
                "        -> Properties:",
                "          -> axis: y",
                "  -> Log Length:",
                "    -> Constant:",
                "      -> Value: 5",
                "  -> Stump Decorators:",
                "    -> Cocoa:",
                "      -> Probability: 0.2",
                "  -> Log Decorators:",
                "    -> Cocoa:",
                "      -> Probability: 0.3"
        ));
    }

    @Test
    public void testBlockBlobConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getBlockBlobConfigurationTooltip(UTILS, new BlockBlobConfiguration(
                Blocks.DIRT.defaultBlockState(),
                BlockPredicate.solid()
        )).build(), List.of(
                "Block Blob:",
                "  -> State:",
                "    -> Block: Dirt",
                "  -> Can Place On:",
                "    -> Solid:",
                "      -> Offset: [0,0,0]"
        ));
    }

    @Test
    public void testEndSpikeConfigurationTooltip() {
        assertTooltip(FeatureConfigurationTooltipUtils.getEndSpikeConfigurationTooltip(UTILS, new EndSpikeConfiguration(
                true,
                List.of(new EndSpikeFeature.EndSpike(1, 2, 5, 30, true)),
                new BlockPos(1, 2, 3)
        )).build(), List.of(
                "End Spike:",
                "  -> Is Crystal Vulnerable: true",
                "  -> Spikes:",
                "    -> Center X: 1",
                "    -> Center Z: 2",
                "    -> Radius: 5",
                "    -> Height: 30",
                "    -> Is Guarded: true",
                "  -> Crystal Beam Target: [1,2,3]"
        ));
    }
}
