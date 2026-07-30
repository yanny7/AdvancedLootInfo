package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.RangeValue;
import org.jetbrains.annotations.Nullable;

/**
 * The partial contribution a single {@code PlacementModifier} makes to the overall summary. Each
 * modifier fills at most one axis (count / chance / height); the rest stay {@code null}.
 */
public record PlacementContribution(@Nullable CountSpan count, @Nullable RangeValue chancePercent, @Nullable HeightSpan height) {
    public static final PlacementContribution EMPTY = new PlacementContribution(null, null, null);

    public static PlacementContribution ofCount(CountSpan count) {
        return new PlacementContribution(count, null, null);
    }

    public static PlacementContribution ofChance(RangeValue chancePercent) {
        return new PlacementContribution(null, chancePercent, null);
    }

    public static PlacementContribution ofHeight(HeightSpan height) {
        return new PlacementContribution(null, null, height);
    }
}
