package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import com.yanny.awi.plugin.common.tooltip.EmptyTooltipNode;
import net.minecraft.util.valueproviders.IntProvider;
import org.jetbrains.annotations.NotNull;

public class IntProviderTooltipUtils {
    @NotNull
    public static ITooltipNode getMissingIntProviderTooltip(IServerUtils utils, IntProvider provider) {
        //TODO auto detected placed feature
        return EmptyTooltipNode.EMPTY;
    }
}
