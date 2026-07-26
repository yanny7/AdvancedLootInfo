package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.RangeValue;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.levelgen.placement.*;
import org.jetbrains.annotations.NotNull;

/**
 * One method per {@code PlacementModifier} type that carries count/chance/height information. Each returns
 * a {@link PlacementContribution} for a single axis and delegates to the int/height span registries where a
 * sub-provider is involved. Modifiers with no useful count/chance/height simply aren't registered (they fall
 * back to {@link PlacementContribution#EMPTY}).
 */
public class PlacementPropagatorUtils {
    @NotNull
    public static PlacementContribution getCountPlacement(IServerUtils utils, CountPlacement placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofCount(utils.getIntSpan(utils, placement.count));
    }

    @NotNull
    public static PlacementContribution getCountOnEveryLayerPlacement(IServerUtils utils, CountOnEveryLayerPlacement placement, ColumnContext ignoredCtx) {
        // count is applied on every solid layer; the total per chunk depends on how many layers exist (unknown here)
        RangeValue perLayer = utils.getIntSpan(utils, placement.count).range();
        return PlacementContribution.ofCount(new CountSpan(markUnknown(perLayer), Kind.UNKNOWN));
    }

    @NotNull
    public static PlacementContribution getNoiseBasedCountPlacement(IServerUtils ignoredUtils, NoiseBasedCountPlacement placement, ColumnContext ignoredCtx) {
        // count is driven by the noise field, roughly within [0, noiseToCountRatio]
        return PlacementContribution.ofCount(new CountSpan(markUnknown(new RangeValue(0, placement.noiseToCountRatio)), Kind.UNKNOWN));
    }

    @NotNull
    public static PlacementContribution getNoiseThresholdCountPlacement(IServerUtils ignoredUtils, NoiseThresholdCountPlacement placement, ColumnContext ignoredCtx) {
        // count is either belowNoise or aboveNoise depending on the local noise value
        int min = Math.min(placement.belowNoise, placement.aboveNoise);
        int max = Math.max(placement.belowNoise, placement.aboveNoise);
        return PlacementContribution.ofCount(new CountSpan(markUnknown(new RangeValue(min, max)), Kind.UNKNOWN));
    }

    /** Keeps the numeric range but flags it uncertain (rendered as {@code [+???]}). */
    @NotNull
    private static RangeValue markUnknown(RangeValue range) {
        return range.multiply(new RangeValue(false, true));
    }

    @NotNull
    public static PlacementContribution getRarityFilter(IServerUtils ignoredUtils, RarityFilter placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofChance(new RangeValue(100f / placement.chance));
    }

    @NotNull
    public static PlacementContribution getHeightRangePlacement(IServerUtils utils, HeightRangePlacement placement, ColumnContext ctx) {
        return PlacementContribution.ofHeight(utils.getHeightSpan(utils, placement.height, ctx));
    }

    @NotNull
    public static PlacementContribution getHeightmapPlacement(IServerUtils ignoredUtils, HeightmapPlacement placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofHeight(HeightSpan.relativeTo(placement.heightmap));
    }

    @NotNull
    public static PlacementContribution getSurfaceRelativeThresholdFilter(IServerUtils ignoredUtils, SurfaceRelativeThresholdFilter placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofHeight(HeightSpan.relativeTo(placement.heightmap));
    }
}
