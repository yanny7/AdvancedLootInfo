package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.NotNull;

public class PlacementModifierTooltipUtils {
    @NotNull
    public static TooltipNode getMissingPlacementModifierTooltip(IServerUtils utils, PlacementModifier configuration) {
        //TODO auto detected placed feature
        return TooltipNode.EMPTY_INSTANCE;
    }
}
