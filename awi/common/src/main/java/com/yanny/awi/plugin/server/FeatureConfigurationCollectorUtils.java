package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FeatureConfigurationCollectorUtils {
    @Unmodifiable
    @NotNull
    public static List<Block> collectBlockColumnConfigurationBlocks(IServerUtils utils, BlockColumnConfiguration configuration) {
        return configuration.layers().stream().map((layer) -> utils.collectBlocks(utils, layer.state())).flatMap(Collection::stream).toList();
        //FIXME for all FeatureConfiguration, there is also getFeatures() -> can contains again FC!!!
        //FIXME - there are different providers - trunkplacers etc...
    }

    @NotNull
    public static List<Block> collectBlockPileConfigurationBlocks(IServerUtils utils, BlockPileConfiguration configuration) {
        return utils.collectBlocks(utils, configuration.stateProvider);
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectBlockStateConfigurationBlocks(IServerUtils ignoredUtils, BlockStateConfiguration configuration) {
        return Collections.singletonList(configuration.state.getBlock());
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectDeltaFeatureConfigurationBlocks(IServerUtils ignoredUtils, DeltaFeatureConfiguration configuration) {
        return List.of(configuration.contents().getBlock(), configuration.rim().getBlock());
    }

    @NotNull
    public static List<Block> collectDiskConfigurationBlocks(IServerUtils utils, DiskConfiguration configuration) {
        return utils.collectBlocks(utils, configuration.stateProvider());
    }

    @NotNull
    public static List<Block> collectHugeMushroomFeatureConfigurationBlocks(IServerUtils utils, HugeMushroomFeatureConfiguration configuration) {
        List<Block> blocks = new ArrayList<>();

        blocks.addAll(utils.collectBlocks(utils, configuration.capProvider()));
        blocks.addAll(utils.collectBlocks(utils, configuration.stemProvider()));
        return blocks;
    }

    @NotNull
    public static List<Block> collectLakeFeatureConfigurationBlocks(IServerUtils utils, @SuppressWarnings("deprecation") LakeFeature.Configuration configuration) {
        List<Block> blocks = new ArrayList<>();

        blocks.addAll(utils.collectBlocks(utils, configuration.barrier()));
        blocks.addAll(utils.collectBlocks(utils, configuration.fluid()));
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectLayeredConfigurationBlocks(IServerUtils ignoredUtils, LayerConfiguration configuration) {
        return Collections.singletonList(configuration.state.getBlock());
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectMultifaceGrowthConfigurationBlocks(IServerUtils ignoredUtils, MultifaceGrowthConfiguration configuration) {
        return Collections.singletonList(configuration.placeBlock);
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectOreConfigurationBlocks(IServerUtils ignoredUtils, OreConfiguration configuration) {
        return configuration.targetStates.stream().map((state) -> state.state.getBlock()).toList();
    }

    @NotNull
    public static List<Block> collectRandomBooleanFeatureConfigurationBlocks(IServerUtils utils, RandomBooleanFeatureConfiguration configuration) {
        List<Block> blocks = new ArrayList<>();

        blocks.addAll(utils.collectBlocks(utils, configuration.featureTrue.value().feature().value().config()));
        blocks.addAll(utils.collectBlocks(utils, configuration.featureFalse.value().feature().value().config()));
        return blocks;
    }

    @NotNull
    public static List<Block> collectRandomFeatureConfigurationBlocks(IServerUtils utils, RandomFeatureConfiguration configuration) {
        List<Block> blocks = new ArrayList<>();

        blocks.addAll(utils.collectBlocks(utils, configuration.defaultFeature.value().feature().value().config()));
        blocks.addAll(configuration.features.stream().map((feature) -> utils.collectBlocks(utils, feature.feature.value().feature().value().config())).flatMap(Collection::stream).toList());
        return blocks;
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectReplaceBlockConfigurationBlocks(IServerUtils ignoredUtils, ReplaceBlockConfiguration configuration) {
        return configuration.targetStates.stream().map((state) -> state.state.getBlock()).toList();
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectReplaceSphereConfigurationBlocks(IServerUtils ignoredUtils, ReplaceSphereConfiguration configuration) {
        return Collections.singletonList(configuration.replaceState.getBlock());
    }

    @NotNull
    public static List<Block> collectRootSystemConfigurationBlocks(IServerUtils utils, RootSystemConfiguration configuration) {
        List<Block> blocks = new ArrayList<>();

        blocks.addAll(utils.collectBlocks(utils, configuration.rootStateProvider));
        blocks.addAll(utils.collectBlocks(utils, configuration.hangingRootStateProvider));
        blocks.addAll(utils.collectBlocks(utils, configuration.treeFeature.value().feature().value().config()));
        return blocks;
    }

    @NotNull
    public static List<Block> collectSimpleBlockConfigurationBlocks(IServerUtils utils, SimpleBlockConfiguration configuration) {
        return utils.collectBlocks(utils, configuration.toPlace());
    }

    @Unmodifiable
    @NotNull
    public static List<Block> collectSpringConfigurationBlocks(IServerUtils ignoredUtils, SpringConfiguration configuration) {
        return Collections.singletonList(configuration.state.createLegacyBlock().getBlock());
    }

    @NotNull
    public static List<Block> collectTreeConfigurationBlocks(IServerUtils utils, TreeConfiguration configuration) {
        List<Block> blocks = new ArrayList<>();

        blocks.addAll(utils.collectBlocks(utils, configuration.trunkProvider));
        blocks.addAll(utils.collectBlocks(utils, configuration.belowTrunkProvider));
        blocks.addAll(utils.collectBlocks(utils, configuration.foliageProvider));
        configuration.rootPlacer.ifPresent((rootPlacer) -> {
            blocks.addAll(utils.collectBlocks(utils, rootPlacer.rootProvider));
            rootPlacer.aboveRootPlacement.ifPresent((aboveRootPlacement) -> blocks.addAll(utils.collectBlocks(utils, aboveRootPlacement.aboveRootProvider())));
        });
//        blocks.addAll(configuration.decorators.stream().map((decorator) -> decorator)) //todo
        return blocks;
    }

    @NotNull
    public static List<Block> collectVegetationPatchConfigurationBlocks(IServerUtils utils, VegetationPatchConfiguration configuration) {
        List<Block> blocks = new ArrayList<>();

        blocks.addAll(utils.collectBlocks(utils, configuration.groundState));
        blocks.addAll(utils.collectBlocks(utils, configuration.vegetationFeature.value().feature().value().config()));
        return blocks;
    }
}
