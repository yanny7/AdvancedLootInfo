package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.api.FeatureHolder;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class PlacedFeatureUtils {
    @NotNull
    public static FeatureHolder getMissingPlacedFeature(IServerUtils utils, PlacedFeature placedFeature) {
        //TODO auto detected placed feature
        return new FeatureHolder(Collections.emptyList(), TooltipNode.empty());
    }
}
