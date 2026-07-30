package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.RangeValue;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

/**
 * The vertical span a feature can generate in.
 *
 * @param range     absolute-Y min..max (unknown when {@link #heightmap} is set)
 * @param bestBand  the contiguous Y interval with the highest chance to find the block; for peaked
 *                  distributions (biased) this collapses to the single mode Y; {@code null} when unknown
 * @param heightmap set only when the height is relative to a terrain heightmap (no absolute Y available)
 * @param kind      distribution shape of the underlying {@code HeightProvider}
 */
public record HeightSpan(RangeValue range, @Nullable RangeValue bestBand, @Nullable Heightmap.Types heightmap, Kind kind) {
    public static HeightSpan of(RangeValue range, RangeValue bestBand, Kind kind) {
        return new HeightSpan(range, bestBand, null, kind);
    }

    public static HeightSpan relativeTo(Heightmap.Types heightmap) {
        return new HeightSpan(new RangeValue(false, true), null, heightmap, Kind.RELATIVE_TO_HEIGHTMAP);
    }

    public static HeightSpan unknown() {
        return new HeightSpan(new RangeValue(false, true), null, null, Kind.UNKNOWN);
    }
}
