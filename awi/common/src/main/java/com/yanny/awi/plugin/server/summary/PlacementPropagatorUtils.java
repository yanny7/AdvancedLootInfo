package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.levelgen.feature.CuboidPlacement;
import net.minecraft.world.level.levelgen.placement.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return PlacementContribution.ofCount(new CountSpan(markUnknown(), Kind.UNKNOWN, unknownDetails(utils, placement)));
    }

    @NotNull
    public static PlacementContribution getNoiseBasedCountPlacement(IServerUtils utils, NoiseBasedCountPlacement placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofCount(new CountSpan(markUnknown(), Kind.UNKNOWN, unknownDetails(utils, placement)));
    }

    @NotNull
    public static PlacementContribution getNoiseThresholdCountPlacement(IServerUtils utils, NoiseThresholdCountPlacement placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofCount(new CountSpan(markUnknown(), Kind.UNKNOWN, unknownDetails(utils, placement)));
    }

    @NotNull
    public static PlacementContribution getCuboidPlacement(IServerUtils utils, CuboidPlacement placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofCount(new CountSpan(markUnknown(), Kind.UNKNOWN, unknownDetails(utils, placement)));
    }

    @NotNull
    public static PlacementContribution getRandomChancePlacement(IServerUtils ignoredUtils, RandomChancePlacement placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofChance(new RangeValue(placement.chance() * 100f));
    }

    /** Only one of the nested modifiers runs, so every axis spans the union of what they contribute. */
    @NotNull
    public static PlacementContribution getRandomlySelectedPlacement(IServerUtils utils, RandomlySelectedPlacement placement, ColumnContext ctx) {
        CountSpan count = null;
        RangeValue chancePercent = null;
        HeightSpan height = null;

        for (PlacementModifier modifier : placement.placements()) {
            PlacementContribution contribution = utils.getPlacementContribution(utils, modifier, ctx);

            count = mergeCount(count, contribution.count());
            chancePercent = (chancePercent == null || contribution.chancePercent() == null)
                    ? (chancePercent != null ? chancePercent : contribution.chancePercent())
                    : chancePercent.union(contribution.chancePercent());
            height = mergeHeight(height, contribution.height());
        }

        return new PlacementContribution(count, chancePercent, height);
    }

    @Nullable
    private static CountSpan mergeCount(@Nullable CountSpan current, @Nullable CountSpan next) {
        if (current == null || next == null) {
            return current != null ? current : next;
        }

        return new CountSpan(current.range().union(next.range()), Kind.UNKNOWN);
    }

    @Nullable
    private static HeightSpan mergeHeight(@Nullable HeightSpan current, @Nullable HeightSpan next) {
        if (current == null || next == null) {
            return current != null ? current : next;
        }

        return HeightSpan.unknown();
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

    @NotNull
    public static PlacementContribution getFixedPlacement(IServerUtils utils, FixedPlacement placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofCount(new CountSpan(markUnknown(), Kind.UNKNOWN, unknownDetails(utils, placement)));
    }

    /** Keeps the numeric range but flags it uncertain (rendered as {@code Unknown}). */
    @NotNull
    private static RangeValue markUnknown() {
        return new RangeValue(false, true);
    }

    /** The modifier's own tooltip (header + parameters), shown nested under the "Unknown" count so it's clear which values it came from. */
    @NotNull
    private static <T extends PlacementModifier> TooltipNode unknownDetails(IServerUtils utils, T placement) {
        return utils.getPlacementModifierTooltip(utils, placement).build();
    }
}
