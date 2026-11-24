package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.Registry;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getBuiltInRegistryTooltip;

public class RegistriesTooltipUtils {
    @NotNull
    public static TooltipBuilder getBlockTooltip(IServerUtils utils, Block block) {
        return getBuiltInRegistryTooltip(utils, Registry.BLOCK, block, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getFluidTooltip(IServerUtils utils, Fluid fluid) {
        return getBuiltInRegistryTooltip(utils, Registry.FLUID, fluid, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getPlacementModifierTooltip(IServerUtils utils, PlacementModifierType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.PLACEMENT_MODIFIERS, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getIntProviderTooltip(IServerUtils utils, IntProviderType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.INT_PROVIDER_TYPES, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getRuleTestTypeTooltip(IServerUtils utils, RuleTestType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.RULE_TEST, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getHeightProviderTooltip(IServerUtils utils, HeightProviderType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.HEIGHT_PROVIDER_TYPES, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getBlockPredicateTooltip(IServerUtils utils, BlockPredicateType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.BLOCK_PREDICATE_TYPES, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getFeatureTypeTooltip(IServerUtils utils, Feature<?> feature) {
        return getBuiltInRegistryTooltip(utils, Registry.FEATURE, feature, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getBlockStateProviderTooltip(IServerUtils utils, BlockStateProviderType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.BLOCKSTATE_PROVIDER_TYPES, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getTreeDecoratorTooltip(IServerUtils utils, TreeDecoratorType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.TREE_DECORATOR_TYPES, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getFeatureSizeTooltip(IServerUtils utils, FeatureSizeType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.FEATURE_SIZE_TYPES, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getRootPlacerTooltip(IServerUtils utils, RootPlacerType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.ROOT_PLACER_TYPES, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getFoliagePlacerTooltip(IServerUtils utils, FoliagePlacerType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.FOLIAGE_PLACER_TYPES, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getTrunkPlacerTooltip(IServerUtils utils, TrunkPlacerType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.TRUNK_PLACER_TYPES, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getFloatProviderTooltip(IServerUtils utils, FloatProviderType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.FLOAT_PROVIDER_TYPES, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getStructureProcessorTypeTooltip(IServerUtils utils, StructureProcessorType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.STRUCTURE_PROCESSOR, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getPosRuleTestTypeTooltip(IServerUtils utils, PosRuleTestType<?> type) {
        return getBuiltInRegistryTooltip(utils, Registry.POS_RULE_TEST, type, utils.getConfiguration().showInGameNames);
    }
}
