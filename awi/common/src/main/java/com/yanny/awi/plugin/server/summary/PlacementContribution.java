package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record PlacementContribution(@Nullable NumberExpr count, List<TooltipNode> countConditions, @Nullable TooltipNode countDetails,
                                    @Nullable NumberExpr chance, List<TooltipNode> chanceConditions, @Nullable HeightSpan height) {
    public static final PlacementContribution EMPTY = new PlacementContribution(null, List.of(), null, null, List.of(), null);

    public PlacementContribution {
        countConditions = List.copyOf(countConditions);
        chanceConditions = List.copyOf(chanceConditions);
    }

    @NotNull
    public static PlacementContribution ofCount(NumberExpr count, List<TooltipNode> conditions) {
        return new PlacementContribution(count, conditions, null, null, List.of(), null);
    }

    @NotNull
    public static PlacementContribution ofCount(NumberExpr count, TooltipNode details) {
        return new PlacementContribution(count, List.of(), details, null, List.of(), null);
    }

    @NotNull
    public static PlacementContribution ofChance(NumberExpr chance, List<TooltipNode> conditions) {
        return new PlacementContribution(null, List.of(), null, chance, conditions, null);
    }

    @NotNull
    public static PlacementContribution ofHeight(HeightSpan height) {
        return new PlacementContribution(null, List.of(), null, null, List.of(), height);
    }
}
