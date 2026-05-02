package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IKeyTooltipNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.plugin.common.tooltip.ErrorTooltipNode;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import org.jetbrains.annotations.NotNull;

public class IntProviderTooltipUtils {
    @NotNull
    public static IKeyTooltipNode getMissingIntProviderTooltip(IServerUtils utils, IntProvider provider) {
        //TODO auto detected placed feature
        return ErrorTooltipNode.error(provider.getClass().getTypeName());
    }

    @NotNull
    public static IKeyTooltipNode getConstantIntTooltip(IServerUtils utils, ConstantInt provider) {
        return utils.getValueTooltip(utils, provider.getValue());
    }

    @NotNull
    public static IKeyTooltipNode getUniformIntTooltip(IServerUtils utils, UniformInt provider) {
        return utils.getValueNode("[" + provider.getMinValue() + "-" + provider.getMaxValue() + "]");
    }
}
