package com.yanny.awi.plugin.server;

import com.mojang.datafixers.util.Either;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class FeatureCollectorUtils {
    /**
     * Blocks of one feature: what the feature itself declares, plus what its {@code place()} bytecode hardcodes.
     * Every descent into a nested feature has to go through here - a nested feature that declares no blocks of its
     * own is dropped entirely otherwise.
     *
     * <p>The scanner's tags travel out as tags rather than as their members, which is why every collector in this
     * class hands back {@code Either}s.
     *
     * <p>Blocks the scanner only reached through a test on the feature's own fields are dropped unless
     * {@link com.yanny.awi.configuration.AwiConfig#showConfigConditionalBlocks} says otherwise. Dropping them here
     * rather than in the widget also keeps them out of the reverse index.
     */
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectFeatureBlocks(IServerUtils utils, Feature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(utils.collectBlocks(utils, feature));
        FeatureBytecodeScanner.ScanResult scan = FeatureBytecodeScanner.scan(feature.getClass());
        Set<Block> scanned = new LinkedHashSet<>(scan.blocks());

        if (!utils.getConfiguration().showConfigConditionalBlocks) {
            scanned.removeAll(scan.configConditionalBlocks());
        }

        blocks.addAll(wrap(scanned));
        scan.tags().forEach((tag) -> blocks.add(Either.right(tag)));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectBlockBlob(IServerUtils utils, BlockBlobFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.add(Either.left(feature.state().getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectBlockColumn(IServerUtils utils, BlockColumnFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        feature.layers().forEach((layer) -> blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, layer.state()))));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectBlockPile(IServerUtils utils, BlockPileFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.stateProvider())));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectCoralClaw(IServerUtils utils, CoralClawFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(collectPlacedFeature(utils, feature.feature()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectCoralTree(IServerUtils utils, CoralTreeFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(collectPlacedFeature(utils, feature.feature()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectDelta(IServerUtils utils, DeltaFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.add(Either.left(feature.contents().getBlock()));
        blocks.add(Either.left(feature.rim().getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectDisk(IServerUtils utils, DiskFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.stateProvider())));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectFallenTree(IServerUtils utils, FallenTreeFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.trunkProvider())));
        blocks.addAll(wrap(feature.stumpDecorators().stream().map((d) -> utils.collectBlocks(utils, d)).flatMap(Collection::stream).toList()));
        blocks.addAll(wrap(feature.logDecorators().stream().map((d) -> utils.collectBlocks(utils, d)).flatMap(Collection::stream).toList()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectFillLayer(IServerUtils utils, FillLayerFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.add(Either.left(feature.state().getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectFossil(IServerUtils utils, FossilFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));
        StructureTemplateManager manager = utils.getServerLevel().getServer().getStructureTemplateManager();

        Stream.concat(feature.fossilStructures().stream(), feature.overlayStructures().stream())
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
    public static List<Either<Block, TagKey<Block>>> collectGeode(IServerUtils utils, GeodeFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));
        GeodeBlockSettings settings = feature.blockSettings();

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, settings.fillingProvider())));
        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, settings.innerLayerProvider())));
        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, settings.alternateInnerLayerProvider())));
        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, settings.middleLayerProvider())));
        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, settings.outerLayerProvider())));
        blocks.addAll(wrap(settings.innerPlacements().stream().map(BlockBehaviour.BlockStateBase::getBlock).toList()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectHugeBrownMushroom(IServerUtils utils, HugeBrownMushroomFeature feature) {
        return collectHugeMushroom(utils, feature);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectHugeRedMushroom(IServerUtils utils, HugeRedMushroomFeature feature) {
        return collectHugeMushroom(utils, feature);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectHugeFungus(IServerUtils utils, HugeFungusFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.add(Either.left(feature.stemState().getBlock()));
        blocks.add(Either.left(feature.hatState().getBlock()));
        blocks.add(Either.left(feature.decorState().getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectIceberg(IServerUtils utils, IcebergFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.add(Either.left(feature.state().getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectLake(IServerUtils utils, LakeFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.fluid())));
        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.barrier())));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectMultifaceGrowth(IServerUtils utils, MultifaceGrowthFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.add(Either.left(feature.placeBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectNetherrackReplaceBlobs(IServerUtils utils, ReplaceBlobsFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.add(Either.left(feature.targetState().getBlock())); //TODO maybe remove this?
        blocks.add(Either.left(feature.replaceState().getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectOre(IServerUtils utils, OreFeature feature) {
        return collectAbstractOre(utils, feature);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectScatteredOre(IServerUtils utils, ScatteredOreFeature feature) {
        return collectAbstractOre(utils, feature);
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectProjectedRandomPatchySquare(IServerUtils utils, ProjectedRandomPatchySquare feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.block())));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectRandomNeighborSpread(IServerUtils utils, RandomNeighborSpreadFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.block())));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectReplaceSingleBlock(IServerUtils utils, ReplaceBlockFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(feature.replacements().stream().map((replacement) -> replacement.state().getBlock()).toList()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectRootSystem(IServerUtils utils, RootSystemFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.rootStateProvider())));
        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.hangingRootStateProvider())));
        blocks.addAll(collectPlacedFeature(utils, feature.treeFeature()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSimpleBlock(IServerUtils utils, SimpleBlockFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.toPlace())));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSingleBlockPillar(IServerUtils utils, SingleBlockPillarFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.block())));
        feature.capFeature().ifPresent((cap) -> blocks.addAll(collectPlacedFeature(utils, cap)));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSpeleothem(IServerUtils utils, SpeleothemFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.add(Either.left(feature.baseBlock().getBlock()));
        blocks.add(Either.left(feature.pointedBlock().getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSpeleothemCluster(IServerUtils utils, SpeleothemClusterFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.add(Either.left(feature.baseBlock().getBlock()));
        blocks.add(Either.left(feature.pointedBlock().getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSpring(IServerUtils utils, SpringFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.add(Either.left(feature.state().createLegacyBlock().getBlock()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSteppedColumnCluster(IServerUtils utils, SteppedColumnClusterFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.block())));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectTree(IServerUtils utils, TreeFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.trunkProvider())));
        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.belowTrunkProvider())));
        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.foliageProvider())));
        feature.rootPlacer().ifPresent((rootPlacer) -> blocks.addAll(wrap(utils.collectBlocks(utils, rootPlacer))));
        blocks.addAll(wrap(feature.decorators().stream().map((d) -> utils.collectBlocks(utils, d)).flatMap(Collection::stream).toList()));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectVegetationPatch(IServerUtils utils, VegetationPatchFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.groundState)));
        blocks.addAll(collectPlacedFeature(utils, feature.vegetationFeature));
        return blocks;
    }

    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectWaterloggedVegetationPatch(IServerUtils utils, WaterloggedVegetationPatchFeature feature) {
        return collectVegetationPatch(utils, feature);
    }

    @Unmodifiable
    @NotNull
    public static List<Either<Block, TagKey<Block>>> collectSubFeatures(IServerUtils utils, Feature feature) {
        return feature.getSubFeatures()
                .filter(Holder::isBound)
                .map((sub) -> collectFeatureBlocks(utils, sub.value()))
                .flatMap(Collection::stream)
                .toList();
    }

    @NotNull
    private static List<Either<Block, TagKey<Block>>> collectAbstractOre(IServerUtils utils, AbstractOreFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(feature.targetStates().stream().map((target) -> target.state().getBlock()).toList()));
        return blocks;
    }

    @NotNull
    private static List<Either<Block, TagKey<Block>>> collectHugeMushroom(IServerUtils utils, AbstractHugeMushroomFeature feature) {
        List<Either<Block, TagKey<Block>>> blocks = new ArrayList<>(collectSubFeatures(utils, feature));

        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.capProvider())));
        blocks.addAll(wrap(BlockStateProviderCollectorUtils.collectHolder(utils, feature.stemProvider())));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    private static List<Either<Block, TagKey<Block>>> collectPlacedFeature(IServerUtils utils, Holder<PlacedFeature> placedFeature) {
        if (!placedFeature.isBound() || !placedFeature.value().feature().isBound()) {
            return List.of();
        }

        return collectFeatureBlocks(utils, placedFeature.value().feature().value());
    }

    /** Lifts plain blocks into the {@code Either} the feature collectors hand back. */
    @Unmodifiable
    @NotNull
    private static List<Either<Block, TagKey<Block>>> wrap(Collection<Block> blocks) {
        return blocks.stream().map(Either::<Block, TagKey<Block>>left).toList();
    }
}
