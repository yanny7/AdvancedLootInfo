package com.yanny.awi.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.TrapezoidFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;

public class FloatProviderTooltipUtils {
    @NotNull
    public static TooltipBuilder getConstantFloatTooltip(IServerUtils utils, ConstantFloat provider) {
        return array((b) -> b.add(utils.getValueTooltip(utils, provider.getValue()).build(Lang.Value.VALUE)), Lang.FloatProvider.CONSTANT);
    }

    @NotNull
    public static TooltipBuilder getUniformFloatTooltip(IServerUtils utils, UniformFloat provider) {
        return array((b) -> b.add(utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toFloatString()).build(Lang.Value.RANGE)), Lang.FloatProvider.UNIFORM);
    }

    @NotNull
    public static TooltipBuilder getTrapezoidFloatTooltip(IServerUtils utils, TrapezoidFloat provider) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, provider.plateau).build(Lang.Value.PLATEAU));
            b.add(utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toFloatString()).build(Lang.Value.RANGE));
        }, Lang.FloatProvider.TRAPEZOID);
    }

    @NotNull
    public static TooltipBuilder getClampedNormalFloatTooltip(IServerUtils utils, ClampedNormalFloat provider) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, provider.mean).build(Lang.Value.MEAN));
            b.add(utils.getValueTooltip(utils, provider.deviation).build(Lang.Value.DEVIATION));
            b.add(utils.getValueTooltip(utils, new RangeValue(provider.getMinValue(), provider.getMaxValue()).toFloatString()).build(Lang.Value.RANGE));
        }, Lang.FloatProvider.CLAMPED_NORMAL);
    }
}
