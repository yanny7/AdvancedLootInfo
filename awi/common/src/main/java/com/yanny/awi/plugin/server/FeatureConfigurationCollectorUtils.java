package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class FeatureConfigurationCollectorUtils {
    @NotNull
    public static List<Block> collectBlockColumnConfigurationBlocks(IServerUtils utils, BlockColumnConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(configuration.layers().stream().map((layer) -> utils.collectBlocks(utils, layer.state())).flatMap(Collection::stream).toList());
        return blocks;
    }

    @NotNull
    public static List<Block> collectBlockPileConfigurationBlocks(IServerUtils utils, BlockPileConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(utils.collectBlocks(utils, configuration.stateProvider));
        return blocks;
    }

    @NotNull
    public static List<Block> collectBlockStateConfigurationBlocks(IServerUtils utils, BlockStateConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(configuration.state.getBlock());
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectColumnFeatureConfigurationBlocks(IServerUtils utils, ColumnFeatureConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectCountConfigurationBlocks(IServerUtils utils, CountConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Block> collectDeltaFeatureConfigurationBlocks(IServerUtils utils, DeltaFeatureConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(configuration.contents().getBlock());
        blocks.add(configuration.rim().getBlock());
        return blocks;
    }

    @NotNull
    public static List<Block> collectDiskConfigurationBlocks(IServerUtils utils, DiskConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(utils.collectBlocks(utils, configuration.stateProvider()));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectDripstoneClusterConfigurationBlocks(IServerUtils utils, DripstoneClusterConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectEndGatewayConfigurationBlocks(IServerUtils utils, EndGatewayConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Block> collectGeodeConfigurationBlocks(IServerUtils utils, GeodeConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(utils.collectBlocks(utils, configuration.geodeBlockSettings.fillingProvider));
        blocks.addAll(utils.collectBlocks(utils, configuration.geodeBlockSettings.innerLayerProvider));
        blocks.addAll(utils.collectBlocks(utils, configuration.geodeBlockSettings.alternateInnerLayerProvider));
        blocks.addAll(utils.collectBlocks(utils, configuration.geodeBlockSettings.middleLayerProvider));
        blocks.addAll(utils.collectBlocks(utils, configuration.geodeBlockSettings.outerLayerProvider));
        blocks.addAll(configuration.geodeBlockSettings.innerPlacements.stream().map(BlockBehaviour.BlockStateBase::getBlock).toList());
        return blocks;
    }

    @NotNull
    public static List<Block> collectHugeMushroomFeatureConfigurationBlocks(IServerUtils utils, HugeMushroomFeatureConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(utils.collectBlocks(utils, configuration.capProvider()));
        blocks.addAll(utils.collectBlocks(utils, configuration.stemProvider()));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectLargeDripstoneConfigurationBlocks(IServerUtils utils, LargeDripstoneConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Block> collectLayeredConfigurationBlocks(IServerUtils utils, LayerConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(configuration.state.getBlock());
        return blocks;
    }

    @NotNull
    public static List<Block> collectMultifaceGrowthConfigurationBlocks(IServerUtils utils, MultifaceGrowthConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(configuration.placeBlock);
        return blocks;
    }

    @NotNull
    public static List<Block> collectNetherForestVegetationConfigurationBlocks(IServerUtils utils, NetherForestVegetationConfig configuration) {
        return collectBlockPileConfigurationBlocks(utils, configuration);
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectNoneFeatureConfigurationBlocks(IServerUtils utils, NoneFeatureConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Block> collectOreConfigurationBlocks(IServerUtils utils, OreConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(configuration.targetStates.stream().map((state) -> state.state.getBlock()).toList());
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectPointedDripstoneConfigurationBlocks(IServerUtils utils, PointedDripstoneConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectProbabilityFeatureConfigurationBlocks(IServerUtils utils, ProbabilityFeatureConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Block> collectRandomBooleanFeatureConfigurationBlocks(IServerUtils utils, RandomBooleanFeatureConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(utils.collectBlocks(utils, configuration.featureTrue.value().feature().value().config()));
        blocks.addAll(utils.collectBlocks(utils, configuration.featureFalse.value().feature().value().config()));
        return blocks;
    }

    @NotNull
    public static List<Block> collectRandomFeatureConfigurationBlocks(IServerUtils utils, RandomFeatureConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(utils.collectBlocks(utils, configuration.defaultFeature().value().feature().value().config()));
        blocks.addAll(configuration.features().stream().map((feature) -> utils.collectBlocks(utils, feature.feature().value().feature().value().config())).flatMap(Collection::stream).toList());
        return blocks;
    }

    @NotNull
    public static List<Block> collectReplaceBlockConfigurationBlocks(IServerUtils utils, ReplaceBlockConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(configuration.targetStates.stream().map((state) -> state.state.getBlock()).toList());
        return blocks;
    }

    @NotNull
    public static List<Block> collectReplaceSphereConfigurationBlocks(IServerUtils utils, ReplaceSphereConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(configuration.targetState.getBlock()); //TODO maybe remove this?
        blocks.add(configuration.replaceState.getBlock());
        return blocks;
    }

    @NotNull
    public static List<Block> collectRootSystemConfigurationBlocks(IServerUtils utils, RootSystemConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(utils.collectBlocks(utils, configuration.rootStateProvider()));
        blocks.addAll(utils.collectBlocks(utils, configuration.hangingRootStateProvider()));
        blocks.addAll(utils.collectBlocks(utils, configuration.treeFeature().value().feature().value().config()));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectSculkPatchConfigurationBlocks(IServerUtils utils, SculkPatchConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Block> collectSimpleBlockConfigurationBlocks(IServerUtils utils, SimpleBlockConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(utils.collectBlocks(utils, configuration.toPlace()));
        return blocks;
    }

    @NotNull
    public static List<Block> collectSimpleRandomFeatureConfigurationBlocks(IServerUtils utils, SimpleRandomFeatureConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(configuration.getSubFeatures().map((v) -> utils.collectBlocks(utils, v.value().config())).flatMap(Collection::stream).toList());
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectSpikeConfigurationBlocks(IServerUtils utils, SpikeConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Block> collectSpringConfigurationBlocks(IServerUtils utils, SpringConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(configuration.state.createLegacyBlock().getBlock());
        return blocks;
    }

    @NotNull
    public static List<Block> collectTreeConfigurationBlocks(IServerUtils utils, TreeConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(utils.collectBlocks(utils, configuration.trunkProvider));
        blocks.addAll(utils.collectBlocks(utils, configuration.belowTrunkProvider));
        blocks.addAll(utils.collectBlocks(utils, configuration.foliageProvider));
        configuration.rootPlacer.ifPresent((rootPlacer) -> blocks.addAll(utils.collectBlocks(utils, rootPlacer)));
        blocks.addAll(configuration.decorators.stream().map((d) -> utils.collectBlocks(utils, d)).flatMap(Collection::stream).toList());
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectTwistingVinesConfigurationBlocks(IServerUtils utils, TwistingVinesConfig configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectUnderwaterMagmaConfigurationBlocks(IServerUtils utils, UnderwaterMagmaConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Block> collectVegetationPatchConfigurationBlocks(IServerUtils utils, VegetationPatchConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(utils.collectBlocks(utils, configuration.groundState()));
        blocks.addAll(utils.collectBlocks(utils, configuration.vegetationFeature().value().feature().value().config()));
        return blocks;
    }

    @NotNull
    public static List<Block> collectLakeFeatureConfigurationBlocks(IServerUtils utils, @SuppressWarnings("deprecation") LakeFeature.Configuration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(utils.collectBlocks(utils, configuration.barrier()));
        blocks.addAll(utils.collectBlocks(utils, configuration.fluid()));
        return blocks;
    }

    @NotNull
    public static List<Block> collectFossilFeatureConfigurationBlocks(IServerUtils utils, FossilFeatureConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));
        StructureTemplateManager manager = utils.getServerLevel().getServer().getStructureManager();

        Stream.concat(configuration.fossilStructures.stream(), configuration.overlayStructures.stream())
                .distinct()
                .forEach((id) -> manager.get(id).ifPresent((template) -> {
                    for (StructureTemplate.Palette palette : template.palettes) { // private field unlocked via awi.accesswidener
                        for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {
                            Block block = info.state().getBlock();

                            if (block != Blocks.AIR && block != Blocks.STRUCTURE_VOID) { // palettes contain structure_void markers
                                blocks.add(block);
                            }
                        }
                    }
                }));

        return blocks;
    }

    @NotNull
    public static List<Block> collectHugeFungusConfigurationBlocks(IServerUtils utils, HugeFungusConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(configuration.stemState.getBlock());
        blocks.add(configuration.hatState.getBlock());
        blocks.add(configuration.decorState.getBlock());
        return blocks;
    }

    @NotNull
    public static List<Block> collectBlockBlobConfigurationBlocks(IServerUtils utils, BlockBlobConfiguration configuration) {
        List<Block> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(configuration.state().getBlock());
        return blocks;
    }

    @Unmodifiable
    @NotNull
    private static List<Block> collectFeatureBlocks(IServerUtils utils, FeatureConfiguration configuration) {
        return configuration.getSubFeatures().map((v) -> utils.collectBlocks(utils, v.value().config())).flatMap(Collection::stream).toList();
    }
}
