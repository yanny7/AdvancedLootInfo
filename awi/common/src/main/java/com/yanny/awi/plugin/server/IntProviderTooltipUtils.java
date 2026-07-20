package com.yanny.awi.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.util.valueproviders.*;
import org.jetbrains.annotations.NotNull;

public class IntProviderTooltipUtils {
    @NotNull
    public static TooltipBuilder getConstantIntTooltip(IServerUtils utils, ConstantInt provider) {
        return utils.getValueTooltip(utils, provider.getValue());
    }

    @NotNull
    public static TooltipBuilder getUniformIntTooltip(IServerUtils utils, UniformInt provider) {
        return utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toIntString() + " (Uniform)");
    }

    @NotNull
    public static TooltipBuilder getBiasedToBottomIntTooltip(IServerUtils utils, BiasedToBottomInt provider) {
        return utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toIntString() + " (Biased To Bottom)");
    }

    @NotNull
    public static TooltipBuilder getClampedIntTooltip(IServerUtils utils, ClampedInt provider) {
        return utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toIntString() + " (Clamp [" + provider.minInclusive + "," + provider.maxInclusive + "])");
    }

    @NotNull
    public static TooltipBuilder getWeightedListIntTooltip(IServerUtils utils, WeightedListInt provider) {
        return utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toIntString() + " (Weighted)"); // TODO improve
    }

    @NotNull
    public static TooltipBuilder getClampedNormalIntTooltip(IServerUtils utils, ClampedNormalInt provider) {
        return utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toIntString() + " (Clamp mean: " + provider.mean + " dev: " + provider.deviation + ")");
    }
}
