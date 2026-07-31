package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.feature.stateproviders.*;
import org.jetbrains.annotations.NotNull;

public class BlockStateProviderTooltipUtils {
    @NotNull
    public static TooltipBuilder getSimpleStateProviderTooltip(IServerUtils utils, SimpleStateProvider provider) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, provider.state).build(Lang.Branch.STATE)), Lang.BlockStateProvider.SIMPLE);
    }

    @NotNull
    public static TooltipBuilder getWeightedStateProviderTooltip(IServerUtils utils, WeightedStateProvider provider) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, provider.weightedList).build(Lang.Branch.WEIGHTED_LIST)), Lang.BlockStateProvider.WEIGHTED);
    }

    @NotNull
    public static TooltipBuilder getNoiseThresholdProviderTooltip(IServerUtils utils, NoiseThresholdProvider placer) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, placer.threshold).build(Lang.Value.THRESHOLD));
            b.add(utils.getValueTooltip(utils, placer.highChance).build(Lang.Value.HIGH_CHANCE));
            b.add(utils.getValueTooltip(utils, placer.defaultState).build(Lang.Branch.DEFAULT_STATE));
            b.add(utils.getValueTooltip(utils, placer.lowStates).build(Lang.Branch.LOW_STATES));
            b.add(utils.getValueTooltip(utils, placer.highStates).build(Lang.Branch.HIGH_STATES));
        }, Lang.BlockStateProvider.NOISE_THRESHOLD);
    }

    @NotNull
    public static TooltipBuilder getNoiseProviderTooltip(IServerUtils utils, NoiseProvider provider) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, provider.states).build(Lang.Branch.STATES)), Lang.BlockStateProvider.NOISE_PROVIDER);
    }

    @NotNull
    public static TooltipBuilder getDualNoiseProviderTooltip(IServerUtils ignoredUtils, DualNoiseProvider ignoredProvider) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, Lang.BlockStateProvider.DUAL_NOISE_PROVIDER);
    }

    @NotNull
    public static TooltipBuilder getRotatedBlockProviderTooltip(IServerUtils utils, RotatedBlockProvider provider) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, provider.block).build(Lang.Value.BLOCK)), Lang.BlockStateProvider.ROTATED_BLOCK);
    }

    @NotNull
    public static TooltipBuilder getRandomizedIntStateProviderTooltip(IServerUtils utils, RandomizedIntStateProvider placer) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, placer.source).build(Lang.Branch.SOURCE));
            b.add(utils.getValueTooltip(utils, placer.propertyName).build(Lang.Value.PROPERTY_NAME));
            b.add(utils.getValueTooltip(utils, placer.values).build(Lang.Branch.VALUES));
        }, Lang.BlockStateProvider.RANDOMIZED_INT_STATE);
    }

    @NotNull
    public static TooltipBuilder getRuleBasedStateProviderTooltip(IServerUtils utils, RuleBasedStateProvider placer) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, placer.fallback).build(Lang.Branch.FALLBACK));
            b.add(utils.getValueTooltip(utils, placer.rules).build(Lang.Branch.RULES));
        }, Lang.BlockStateProvider.RULE_BASED);
    }
}
