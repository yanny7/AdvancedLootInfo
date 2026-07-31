package com.yanny.awi.plugin.server;

import com.mojang.logging.LogUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getBuiltInRegistryTooltip;

public class RegistriesTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static TooltipBuilder getBlockTooltip(IServerUtils utils, Block block) {
//        if (utils.getConfiguration().showInGameNames) { FIXME
            try {
                return TooltipBuilder.value(TooltipBuilder.translate(block.getDescriptionId()));
            } catch (Throwable e) {
                LOGGER.warn("Failed to get localized Block name: {}", BuiltInRegistries.BLOCK.getKey(block), e);
            }
//        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCK, block);
    }

    @NotNull
    public static TooltipBuilder getFluidTooltip(IServerUtils utils, Fluid fluid) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.FLUID, fluid);
    }

    @NotNull
    public static TooltipBuilder getPlacementModifierTooltip(IServerUtils utils, PlacementModifierType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getIntProviderTooltip(IServerUtils utils, IntProvider type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.INT_PROVIDER_TYPE, type.codec());
    }

    @NotNull
    public static TooltipBuilder getRuleTestTypeTooltip(IServerUtils utils, RuleTestType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.RULE_TEST, type);
    }

    @NotNull
    public static TooltipBuilder getHeightProviderTooltip(IServerUtils utils, HeightProviderType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.HEIGHT_PROVIDER_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getBlockPredicateTooltip(IServerUtils utils, BlockPredicateType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCK_PREDICATE_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getFeatureTypeTooltip(IServerUtils utils, Feature<?> feature) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.FEATURE, feature);
    }

    @NotNull
    public static TooltipBuilder getBlockStateProviderTooltip(IServerUtils utils, BlockStateProviderType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getTreeDecoratorTooltip(IServerUtils utils, TreeDecoratorType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.TREE_DECORATOR_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getFeatureSizeTooltip(IServerUtils utils, FeatureSizeType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.FEATURE_SIZE_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getRootPlacerTooltip(IServerUtils utils, RootPlacerType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ROOT_PLACER_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getFoliagePlacerTooltip(IServerUtils utils, FoliagePlacerType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.FOLIAGE_PLACER_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getTrunkPlacerTooltip(IServerUtils utils, TrunkPlacerType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.TRUNK_PLACER_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getFloatProviderTooltip(IServerUtils utils, FloatProvider type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.FLOAT_PROVIDER_TYPE, type.codec());
    }

    @NotNull
    public static TooltipBuilder getPosRuleTestTypeTooltip(IServerUtils utils, PosRuleTestType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.POS_RULE_TEST, type);
    }

    @NotNull
    public static TooltipBuilder getRuleBlockEntityModifierTooltip(IServerUtils utils, RuleBlockEntityModifierType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.RULE_BLOCK_ENTITY_MODIFIER, type);
    }
}
