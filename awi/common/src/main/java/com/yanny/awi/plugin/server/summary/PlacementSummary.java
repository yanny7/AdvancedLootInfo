package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import org.jetbrains.annotations.Nullable;

public record PlacementSummary(@Nullable NumberExpr count, @Nullable TooltipNode countDetails, @Nullable NumberExpr chance, @Nullable HeightSpan height) {
}
