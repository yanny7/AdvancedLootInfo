package com.yanny.awi.test.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.*;
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
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yanny.awi.test.TooltipTestSuite.LOOKUP;

public class FeatureInstances {
    private static final Holder<PlacedFeature> PLACED_FEATURE = Holder.direct(new PlacedFeature(
            Holder.direct(new NoOpFeature()),
            List.of(BlockPredicateFilter.forPredicate(BlockPredicate.solid()))
    ));

    private static final HolderSet<net.minecraft.world.level.block.Block> WOOL =
            LOOKUP.lookupOrThrow(Registries.BLOCK).getOrThrow(BlockTags.WOOL);

    /**
     * One instance per registered feature type, keyed by its {@code FEATURE_TYPE} id. Vanilla data instantiates only
     * 47 of the 58 types, so anything driven by the data registry alone would silently skip the rest - including
     * {@code sculk_patch} and the corals, which the bytecode scanner treats as showcase cases.
     */
    @NotNull
    public static Map<String, Feature> features() {
        Map<String, Feature> features = new LinkedHashMap<>();

        features.put("bamboo", new BambooFeature(0.25f));
        features.put("block_blob", new BlockBlobFeature(Blocks.MAGMA_BLOCK.defaultBlockState(), BlockPredicate.solid()));
        features.put("block_column", new BlockColumnFeature(
                List.of(BlockColumnFeature.layer(ConstantInt.of(2), BlockStateProvider.of(Blocks.CAVE_VINES))),
                Direction.DOWN, BlockPredicate.ONLY_IN_AIR_PREDICATE, true));
        features.put("block_pile", new BlockPileFeature(BlockStateProvider.holderOf(Blocks.HAY_BLOCK)));
        features.put("blue_ice", new BlueIceFeature());
        features.put("bonus_chest", new BonusChestFeature());
        features.put("chorus_plant", new ChorusPlantFeature());
        features.put("coral_claw", new CoralClawFeature(PLACED_FEATURE));
        features.put("coral_tree", new CoralTreeFeature(PLACED_FEATURE));
        features.put("delta_feature", new DeltaFeature(
                Blocks.LAVA.defaultBlockState(), Blocks.MAGMA_BLOCK.defaultBlockState(), ConstantInt.of(4), UniformInt.of(1, 2)));
        features.put("disk", new DiskFeature(
                BlockStateProvider.holderOf(Blocks.SAND), BlockPredicate.solid(), ConstantInt.of(3), 2));
        features.put("end_gateway", new EndGatewayFeature(Optional.of(new BlockPos(1, 2, 3)), true));
        features.put("end_island", new EndIslandFeature());
        features.put("end_platform", new EndPlatformFeature());
        features.put("end_podium", new EndPodiumFeature(true));
        features.put("end_spike", new EndSpikeFeature(
                List.of(new EndSpikeFeature.EndSpike(1, 2, 5, 30, true)), true, Optional.of(new BlockPos(1, 2, 3))));
        features.put("fallen_tree", new FallenTreeFeature(
                BlockStateProvider.holderOf(Blocks.OAK_LOG), ConstantInt.of(5),
                List.of(new TrunkVineDecorator()), List.of(new CocoaDecorator(0.5f))));
        features.put("fill_layer", new FillLayerFeature(4, Blocks.STONE.defaultBlockState()));
        features.put("fossil", new FossilFeature(
                List.of(Identifier.fromNamespaceAndPath("minecraft", "fossil/spine_1")),
                List.of(Identifier.fromNamespaceAndPath("minecraft", "fossil_spine_1")),
                Holder.direct(new StructureProcessorList(List.of(BlockIgnoreProcessor.AIR))),
                Holder.direct(new StructureProcessorList(List.of(BlockIgnoreProcessor.AIR))),
                3));
        features.put("freeze_top_layer", new SnowAndFreezeFeature());
        features.put("geode", new GeodeFeature(
                new GeodeBlockSettings(
                        BlockStateProvider.holderOf(Blocks.AIR),
                        BlockStateProvider.holderOf(Blocks.AMETHYST_BLOCK),
                        BlockStateProvider.holderOf(Blocks.BUDDING_AMETHYST),
                        BlockStateProvider.holderOf(Blocks.CALCITE),
                        BlockStateProvider.holderOf(Blocks.SMOOTH_BASALT),
                        List.of(Blocks.AMETHYST_CLUSTER.defaultBlockState()),
                        WOOL,
                        LOOKUP.lookupOrThrow(Registries.BLOCK).getOrThrow(BlockTags.LOGS)),
                new GeodeLayerSettings(1.7, 2.2, 3.2, 4.2),
                new GeodeCrackSettings(1.0, 2.0, 2),
                0.35, 0.0, true,
                ConstantInt.of(5), ConstantInt.of(4), ConstantInt.of(2),
                -16, 16, 0.05, 1));
        features.put("huge_brown_mushroom", new HugeBrownMushroomFeature(
                BlockStateProvider.holderOf(Blocks.BROWN_MUSHROOM_BLOCK), BlockStateProvider.holderOf(Blocks.MUSHROOM_STEM),
                3, BlockPredicate.solid()));
        features.put("huge_fungus", new HugeFungusFeature(
                Blocks.CRIMSON_NYLIUM.defaultBlockState(), Blocks.CRIMSON_STEM.defaultBlockState(),
                Blocks.NETHER_WART_BLOCK.defaultBlockState(), Blocks.SHROOMLIGHT.defaultBlockState(),
                BlockPredicate.solid(), true));
        features.put("huge_red_mushroom", new HugeRedMushroomFeature(
                BlockStateProvider.holderOf(Blocks.RED_MUSHROOM_BLOCK), BlockStateProvider.holderOf(Blocks.MUSHROOM_STEM),
                2, BlockPredicate.solid()));
        features.put("iceberg", new IcebergFeature(Blocks.PACKED_ICE.defaultBlockState()));
        features.put("lake", new LakeFeature(
                BlockStateProvider.holderOf(Blocks.WATER), BlockStateProvider.holderOf(Blocks.STONE),
                BlockPredicate.solid(), BlockPredicate.solid(), BlockPredicate.solid()));
        features.put("large_dripstone", new LargeDripstoneFeature(
                WOOL, 30, ConstantInt.of(2), ConstantFloat.of(0.4f), 0.2f,
                ConstantFloat.of(0.1f), ConstantFloat.of(0.2f), ConstantFloat.of(0.3f), 3, 0.4f));
        features.put("monster_room", new MonsterRoomFeature());
        features.put("multiface_growth", new MultifaceGrowthFeature(
                (MultifaceSpreadeableBlock) Blocks.GLOW_LICHEN, 20, true, true, true, 0.5f, WOOL));
        features.put("netherrack_replace_blobs", new ReplaceBlobsFeature(
                Blocks.NETHERRACK.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState(), UniformInt.of(3, 5)));
        features.put("no_op", new NoOpFeature());
        features.put("ore", new OreFeature(
                List.of(new BlockReplacement(AlwaysTrueTest.INSTANCE, Blocks.FURNACE.defaultBlockState())), 5, 0.5f));
        features.put("overlay", new OverlayFeature(HolderSet.direct(PLACED_FEATURE)));
        features.put("projected_random_patchy_square", new ProjectedRandomPatchySquare(
                BlockStateProvider.holderOf(Blocks.MOSS_CARPET), BlockPredicate.solid(), ConstantInt.of(3), 5));
        features.put("random_boolean_selector", new RandomBooleanSelectorFeature(PLACED_FEATURE, PLACED_FEATURE));
        features.put("random_neighbor_spread", new RandomNeighborSpreadFeature(
                BlockStateProvider.holderOf(Blocks.GLOWSTONE), WOOL, BlockPredicate.solid(),
                ConstantInt.of(1500), UniformInt.of(0, 12), UniformInt.of(0, 12)));
        features.put("random_selector", new RandomSelectorFeature(
                List.of(new WeightedPlacedFeature(PLACED_FEATURE, 0.25f)), PLACED_FEATURE));
        features.put("replace_single_block", new ReplaceBlockFeature(
                List.of(new BlockReplacement(AlwaysTrueTest.INSTANCE, Blocks.EMERALD_ORE.defaultBlockState()))));
        features.put("root_system", new RootSystemFeature(
                PLACED_FEATURE, 3, 2, 1, 4, WOOL, BlockStateProvider.holderOf(Blocks.MANGROVE_ROOTS),
                5, 6, 7, 8, BlockStateProvider.holderOf(Blocks.HANGING_ROOTS), 9, 10, BlockPredicate.solid()));
        features.put("scattered_ore", new ScatteredOreFeature(
                List.of(new BlockReplacement(AlwaysTrueTest.INSTANCE, Blocks.GOLD_ORE.defaultBlockState())), 3, 0.0f));
        features.put("sculk_patch", new SculkPatchFeature(2, 3, 4, 5, 6));
        features.put("sequence", new SequenceFeature(HolderSet.direct(PLACED_FEATURE)));
        features.put("simple_block", new SimpleBlockFeature(BlockStateProvider.holderOf(Blocks.SEAGRASS), true));
        features.put("simple_random_selector", new SimpleRandomSelectorFeature(HolderSet.direct(PLACED_FEATURE)));
        features.put("single_block_pillar", new SingleBlockPillarFeature(
                BlockStateProvider.holderOf(Blocks.BASALT), BlockPredicate.solid(), Direction.UP, 0.75f,
                Optional.of(PLACED_FEATURE)));
        features.put("speleothem", new SpeleothemFeature(
                Blocks.DRIPSTONE_BLOCK.defaultBlockState(), Blocks.POINTED_DRIPSTONE.defaultBlockState(), WOOL,
                0.2f, 0.7f, 0.5f, 0.5f));
        features.put("speleothem_cluster", new SpeleothemClusterFeature(
                Blocks.DRIPSTONE_BLOCK.defaultBlockState(), Blocks.POINTED_DRIPSTONE.defaultBlockState(), WOOL,
                12, UniformInt.of(3, 6), UniformInt.of(2, 8), 1, 3, ConstantInt.of(1),
                ConstantFloat.of(0.4f), ConstantFloat.of(0.3f), 0.1f, 4, 5));
        features.put("spike", new SpikeFeature(
                Blocks.OBSIDIAN.defaultBlockState(), BlockPredicate.solid(), BlockPredicate.solid()));
        features.put("spring_feature", new SpringFeature(
                Fluids.WATER.defaultFluidState(), true, 4, 1, WOOL));
        features.put("stepped_column_cluster", new SteppedColumnClusterFeature(
                BlockStateProvider.holderOf(Blocks.BASALT), BlockPredicate.solid(), BlockPredicate.solid(), WOOL,
                ConstantInt.of(4), UniformInt.of(1, 3), ConstantInt.of(2), UniformInt.of(1, 5)));
        features.put("template", new TemplateFeature(
                WeightedList.of(List.of(new Weighted<>(new TemplateFeature.TemplateEntry(
                        Identifier.fromNamespaceAndPath("minecraft", "trail_ruins/tower/tower_1"),
                        List.of(Rotation.NONE, Rotation.CLOCKWISE_90)), 2))),
                Optional.of(Holder.direct(new StructureProcessorList(List.of(BlockIgnoreProcessor.AIR))))));
        features.put("tree", new TreeFeature(
                BlockStateProvider.holderOf(Blocks.OAK_LOG), new StraightTrunkPlacer(4, 2, 0),
                BlockStateProvider.holderOf(Blocks.OAK_LEAVES), new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                Optional.of(new MangroveRootPlacer(ConstantInt.of(1), BlockStateProvider.holderOf(Blocks.MANGROVE_ROOTS),
                        Optional.empty(), new MangroveRootPlacement(WOOL, WOOL,
                        BlockStateProvider.holderOf(Blocks.MUDDY_MANGROVE_ROOTS), 8, 15, 0.2f))),
                new TwoLayersFeatureSize(1, 0, 1), List.of(new TrunkVineDecorator()), true,
                BlockStateProvider.holderOf(Blocks.DIRT)));
        features.put("underwater_magma", new UnderwaterMagmaFeature(5, 3, 0.5f));
        features.put("vegetation_patch", new VegetationPatchFeature(
                WOOL, BlockStateProvider.holderOf(Blocks.MOSS_BLOCK), PLACED_FEATURE, CaveSurface.FLOOR,
                ConstantInt.of(1), 0.0f, 5, 0.8f, UniformInt.of(1, 2), 0.7f));
        features.put("vines", new VinesFeature());
        features.put("void_start_platform", new VoidStartPlatformFeature());
        features.put("waterlogged_vegetation_patch", new WaterloggedVegetationPatchFeature(
                WOOL, BlockStateProvider.holderOf(Blocks.CLAY), PLACED_FEATURE, CaveSurface.FLOOR,
                ConstantInt.of(1), 0.0f, 5, 0.8f, UniformInt.of(1, 2), 0.7f));
        features.put("weighted_random_selector", new WeightedRandomSelectorFeature(
                WeightedList.of(List.of(new Weighted<>(PLACED_FEATURE, 3)))));

        return features;
    }
}
