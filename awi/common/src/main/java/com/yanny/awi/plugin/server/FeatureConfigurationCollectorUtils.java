package com.yanny.awi.plugin.server;

import com.mojang.datafixers.util.Either;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
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
    public static List<Either<Block, TagKey<Block>>> collectBlockColumnConfigurationBlocks(IServerUtils utils, BlockColumnConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(configuration.layers().stream().map((layer) -> utils.collectBlocks(utils, layer.state())).flatMap(Collection::stream).toList()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectBlockPileConfigurationBlocks(IServerUtils utils, BlockPileConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.stateProvider)));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectBlockStateConfigurationBlocks(IServerUtils utils, BlockStateConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(Either.left(configuration.state.getBlock()));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectColumnFeatureConfigurationBlocks(IServerUtils utils, ColumnFeatureConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectCountConfigurationBlocks(IServerUtils utils, CountConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectDeltaFeatureConfigurationBlocks(IServerUtils utils, DeltaFeatureConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(Either.left(configuration.contents().getBlock()));
        blocks.add(Either.left(configuration.rim().getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectDiskConfigurationBlocks(IServerUtils utils, DiskConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.stateProvider())));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectDripstoneClusterConfigurationBlocks(IServerUtils utils, DripstoneClusterConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectEndGatewayConfigurationBlocks(IServerUtils utils, EndGatewayConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectGeodeConfigurationBlocks(IServerUtils utils, GeodeConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.geodeBlockSettings.fillingProvider)));
        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.geodeBlockSettings.innerLayerProvider)));
        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.geodeBlockSettings.alternateInnerLayerProvider)));
        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.geodeBlockSettings.middleLayerProvider)));
        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.geodeBlockSettings.outerLayerProvider)));
        blocks.addAll(wrap(configuration.geodeBlockSettings.innerPlacements.stream().map(BlockBehaviour.BlockStateBase::getBlock).toList()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectHugeMushroomFeatureConfigurationBlocks(IServerUtils utils, HugeMushroomFeatureConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.capProvider())));
        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.stemProvider())));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectLargeDripstoneConfigurationBlocks(IServerUtils utils, LargeDripstoneConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectLayeredConfigurationBlocks(IServerUtils utils, LayerConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(Either.left(configuration.state.getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectMultifaceGrowthConfigurationBlocks(IServerUtils utils, MultifaceGrowthConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(Either.left(configuration.placeBlock));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectNetherForestVegetationConfigurationBlocks(IServerUtils utils, NetherForestVegetationConfig configuration) {
        return collectBlockPileConfigurationBlocks(utils, configuration);
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectNoneFeatureConfigurationBlocks(IServerUtils utils, NoneFeatureConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectOreConfigurationBlocks(IServerUtils utils, OreConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(configuration.targetStates.stream().map((state) -> state.state.getBlock()).toList()));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectPointedDripstoneConfigurationBlocks(IServerUtils utils, PointedDripstoneConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectProbabilityFeatureConfigurationBlocks(IServerUtils utils, ProbabilityFeatureConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectRandomBooleanFeatureConfigurationBlocks(IServerUtils utils, RandomBooleanFeatureConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(collectConfiguredFeatureBlocks(utils, configuration.featureTrue.value().feature().value()));
        blocks.addAll(collectConfiguredFeatureBlocks(utils, configuration.featureFalse.value().feature().value()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectRandomFeatureConfigurationBlocks(IServerUtils utils, RandomFeatureConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(collectConfiguredFeatureBlocks(utils, configuration.defaultFeature.value().feature().value()));
        blocks.addAll(configuration.features.stream().map((feature) -> collectConfiguredFeatureBlocks(utils, feature.feature.value().feature().value())).flatMap(Collection::stream).toList());
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectReplaceBlockConfigurationBlocks(IServerUtils utils, ReplaceBlockConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(configuration.targetStates.stream().map((state) -> state.state.getBlock()).toList()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectReplaceSphereConfigurationBlocks(IServerUtils utils, ReplaceSphereConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(Either.left(configuration.targetState.getBlock())); //TODO maybe remove this?
        blocks.add(Either.left(configuration.replaceState.getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectRootSystemConfigurationBlocks(IServerUtils utils, RootSystemConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.rootStateProvider)));
        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.hangingRootStateProvider)));
        blocks.addAll(collectConfiguredFeatureBlocks(utils, configuration.treeFeature.value().feature().value()));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSculkPatchConfigurationBlocks(IServerUtils utils, SculkPatchConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSimpleBlockConfigurationBlocks(IServerUtils utils, SimpleBlockConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.toPlace())));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSimpleRandomFeatureConfigurationBlocks(IServerUtils utils, SimpleRandomFeatureConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSpikeConfigurationBlocks(IServerUtils utils, SpikeConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSpringConfigurationBlocks(IServerUtils utils, SpringConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(Either.left(configuration.state.createLegacyBlock().getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectTreeConfigurationBlocks(IServerUtils utils, TreeConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.trunkProvider)));
        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.belowTrunkProvider)));
        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.foliageProvider)));
        configuration.rootPlacer.ifPresent((rootPlacer) -> blocks.addAll(wrap(utils.collectBlocks(utils, rootPlacer))));
        blocks.addAll(wrap(configuration.decorators.stream().map((d) -> utils.collectBlocks(utils, d)).flatMap(Collection::stream).toList()));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectTwistingVinesConfigurationBlocks(IServerUtils utils, TwistingVinesConfig configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectUnderwaterMagmaConfigurationBlocks(IServerUtils utils, UnderwaterMagmaConfiguration configuration) {
        return collectFeatureBlocks(utils, configuration);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectVegetationPatchConfigurationBlocks(IServerUtils utils, VegetationPatchConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.groundState)));
        blocks.addAll(collectConfiguredFeatureBlocks(utils, configuration.vegetationFeature.value().feature().value()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectLakeFeatureConfigurationBlocks(IServerUtils utils, @SuppressWarnings("deprecation") LakeFeature.Configuration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.barrier())));
        blocks.addAll(wrap(utils.collectBlocks(utils, configuration.fluid())));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectFossilFeatureConfigurationBlocks(IServerUtils utils, FossilFeatureConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));
        StructureTemplateManager manager = utils.getServerLevel().getServer().getStructureManager();

        Stream.concat(configuration.fossilStructures.stream(), configuration.overlayStructures.stream())
                .distinct()
                .forEach((id) -> manager.get(id).ifPresent((template) -> {
                    for (StructureTemplate.Palette palette : template.palettes) { // private field unlocked via awi.accesswidener
                        for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {
                            Block block = info.state().getBlock();

                            if (block != Blocks.AIR && block != Blocks.STRUCTURE_VOID) { // palettes contain structure_void markers
                                blocks.add(Either.left(block));
                            }
                        }
                    }
                }));

        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectHugeFungusConfigurationBlocks(IServerUtils utils, HugeFungusConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(Either.left(configuration.stemState.getBlock()));
        blocks.add(Either.left(configuration.hatState.getBlock()));
        blocks.add(Either.left(configuration.decorState.getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectBlockBlobConfigurationBlocks(IServerUtils utils, BlockBlobConfiguration configuration) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectFeatureBlocks(utils, configuration));

        blocks.add(Either.left(configuration.state().getBlock()));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    private static List<Either<Block, TagKey<Block>>> collectFeatureBlocks(IServerUtils utils, FeatureConfiguration configuration) {
        return configuration.getSubFeatures().map((feature) -> collectConfiguredFeatureBlocks(utils, feature.value())).flatMap(Collection::stream).toList();
    }

    /**
     * Blocks of one configured feature: what its configuration declares, plus what its {@code place()} bytecode
     * hardcodes. Every descent into a nested feature has to go through here - a nested feature whose configuration
     * carries no blocks of its own is dropped entirely otherwise. That is not a corner case: warm ocean's coral
     * reaches the world only as three {@code NoneFeatureConfiguration} features nested inside
     * {@code warm_ocean_vegetation}, so scanning just the outer feature finds nothing at all.
     *
     * <p>The scanner's tags travel out as tags rather than as their members, which is also why every collector in this
     * class hands back {@code Either}s: the coral tags are found this deep in the nesting, and flattening them here
     * would leave the display nothing to name the slot after.
     */
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectConfiguredFeatureBlocks(IServerUtils utils, ConfiguredFeature<?, ?> feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(utils.collectBlocks(utils, feature.config()));
        FeatureBytecodeScanner.ScanResult scan = FeatureBytecodeScanner.scan(feature.feature().getClass());

        blocks.addAll(wrap(scan.blocks()));
        scan.tags().forEach((tag) -> blocks.add(Either.right(tag)));
        return blocks;
    }

    /** Lifts plain blocks into the {@code Either} the feature collectors hand back. */
    @Unmodifiable
    @NotNull
    private static List<Either<Block, TagKey<Block>>> wrap(Collection<Block> blocks) {
        return blocks.stream().map(Either::<Block, TagKey<Block>>left).toList();
    }
}
