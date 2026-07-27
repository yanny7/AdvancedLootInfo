package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.RangeValue;

/**
 * How many placements a feature attempts per chunk, plus the shape of that count's distribution.
 *
 * @param range min..max count
 * @param kind  distribution shape of the underlying {@code IntProvider}
 */
public record CountSpan(RangeValue range, Kind kind) {
    public static CountSpan unknown(RangeValue range) {
        return new CountSpan(range, Kind.UNKNOWN);
    }
}
