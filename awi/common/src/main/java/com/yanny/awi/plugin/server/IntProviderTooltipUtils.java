package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import org.jetbrains.annotations.NotNull;

public class IntProviderTooltipUtils {
    @NotNull
    public static TooltipBuilder getMissingIntProviderTooltip(IServerUtils utils, IntProvider provider) {
        //TODO auto detected placed feature
        return TooltipBuilder.error(provider.getClass().getTypeName());
    }

    @NotNull
    public static TooltipBuilder getConstantIntTooltip(IServerUtils utils, ConstantInt provider) {
        return utils.getValueTooltip(utils, provider.value());
    }

    @NotNull
    public static TooltipBuilder getUniformIntTooltip(IServerUtils utils, UniformInt provider) {
        return TooltipBuilder.value("[" + provider.minInclusive() + "-" + provider.maxInclusive() + "]");
    }
}
