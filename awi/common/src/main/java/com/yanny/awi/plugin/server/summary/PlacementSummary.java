package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record PlacementSummary(@Nullable NumberExpr count, List<TooltipNode> countConditions, @Nullable TooltipNode countDetails,
                               @Nullable NumberExpr chance, List<TooltipNode> chanceConditions, @Nullable HeightSpan height) {
}
