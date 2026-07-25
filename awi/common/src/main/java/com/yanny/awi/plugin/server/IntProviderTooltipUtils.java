package com.yanny.awi.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.util.valueproviders.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;

public class IntProviderTooltipUtils {
    @NotNull
    public static TooltipBuilder getConstantIntTooltip(IServerUtils utils, ConstantInt provider) {
        return array((b) -> b.add(utils.getValueTooltip(utils, provider.getValue()).build(Lang.Value.VALUE)), Lang.IntProvider.CONSTANT);
    }

    @NotNull
    public static TooltipBuilder getUniformIntTooltip(IServerUtils utils, UniformInt provider) {
        return array((b) -> b.add(utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toIntString()).build(Lang.Value.RANGE)), Lang.IntProvider.UNIFORM);
    }

    @NotNull
    public static TooltipBuilder getBiasedToBottomIntTooltip(IServerUtils utils, BiasedToBottomInt provider) {
        return array((b) -> b.add(utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toIntString()).build(Lang.Value.RANGE)), Lang.IntProvider.BIASED_TO_BOTTOM);
    }

    @NotNull
    public static TooltipBuilder getClampedIntTooltip(IServerUtils utils, ClampedInt provider) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, provider.source).build(Lang.Branch.SOURCE));
            b.add(utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toIntString()).build(Lang.Value.RANGE));
        }, Lang.IntProvider.CLAMPED);
    }

    @NotNull
    public static TooltipBuilder getWeightedListIntTooltip(IServerUtils utils, WeightedListInt provider) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, provider.distribution.totalWeight).build(Lang.Value.TOTAL_WEIGHT));
            b.add(utils.getValueTooltip(utils, provider.distribution.unwrap()).build(Lang.Branch.ITEMS));
            b.add(utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toIntString()).build(Lang.Value.RANGE));
        }, Lang.IntProvider.WEIGHTED_LIST);
    }

    @NotNull
    public static TooltipBuilder getClampedNormalIntTooltip(IServerUtils utils, ClampedNormalInt provider) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, provider.mean).build(Lang.Value.MEAN));
            b.add(utils.getValueTooltip(utils, provider.deviation).build(Lang.Value.DEVIATION));
            b.add(utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toIntString()).build(Lang.Value.RANGE));
        }, Lang.IntProvider.CLAMPED_NORMAL);
    }
}
