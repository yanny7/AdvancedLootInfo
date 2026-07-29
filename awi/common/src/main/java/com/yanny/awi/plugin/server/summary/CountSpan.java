package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import org.jetbrains.annotations.Nullable;

/**
 * How many placements a feature attempts per chunk, plus the shape of that count's distribution.
 *
 * @param range   min..max count
 * @param kind    distribution shape of the underlying {@code IntProvider}
 * @param details the modifier's own tooltip, worth showing when {@code range} is unknown (e.g. the
 *                noise parameters that drove a {@code NoiseBasedCountPlacement}); {@code null} otherwise
 */
public record CountSpan(RangeValue range, Kind kind, @Nullable TooltipNode details) {
    public CountSpan(RangeValue range, Kind kind) {
        this(range, kind, null);
    }

    public static CountSpan unknown(RangeValue range) {
        return new CountSpan(range, Kind.UNKNOWN);
    }
}
