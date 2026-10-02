package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PlacementContribution(@Nullable NumberExpr count, @Nullable TooltipNode countDetails, @Nullable NumberExpr chance, @Nullable HeightSpan height) {
    public static final PlacementContribution EMPTY = new PlacementContribution(null, null, null, null);

    @NotNull
    public static PlacementContribution ofCount(NumberExpr count) {
        return new PlacementContribution(count, null, null, null);
    }

    @NotNull
    public static PlacementContribution ofCount(NumberExpr count, TooltipNode details) {
        return new PlacementContribution(count, details, null, null);
    }

    @NotNull
    public static PlacementContribution ofChance(NumberExpr chance) {
        return new PlacementContribution(null, null, chance, null);
    }

    @NotNull
    public static PlacementContribution ofHeight(HeightSpan height) {
        return new PlacementContribution(null, null, null, height);
    }
}
