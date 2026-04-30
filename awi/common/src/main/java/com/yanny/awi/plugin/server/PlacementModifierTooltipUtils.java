package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import com.yanny.awi.plugin.common.tooltip.EmptyTooltipNode;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.NotNull;

public class PlacementModifierTooltipUtils {
    @NotNull
    public static ITooltipNode getMissingPlacementModifierTooltip(IServerUtils utils, PlacementModifier configuration) {
        //TODO auto detected placed feature
        return EmptyTooltipNode.EMPTY;
    }
}
