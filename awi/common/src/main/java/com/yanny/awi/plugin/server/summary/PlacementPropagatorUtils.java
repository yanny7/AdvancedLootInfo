package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.CuboidPlacement;
import net.minecraft.world.level.levelgen.placement.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class PlacementPropagatorUtils {
    @NotNull
    public static PlacementContribution getCountPlacement(IServerUtils utils, CountPlacement placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofCount(utils.convertIntProvider(utils, placement.count));
    }

    @NotNull
    public static PlacementContribution getCountOnEveryLayerPlacement(IServerUtils utils, CountOnEveryLayerPlacement placement, ColumnContext ignoredCtx) {
        return opaqueCount(utils, placement);
    }

    @NotNull
    public static PlacementContribution getNoiseBasedCountPlacement(IServerUtils utils, NoiseBasedCountPlacement placement, ColumnContext ignoredCtx) {
        return opaqueCount(utils, placement);
    }

    @NotNull
    public static PlacementContribution getNoiseThresholdCountPlacement(IServerUtils utils, NoiseThresholdCountPlacement placement, ColumnContext ignoredCtx) {
        return opaqueCount(utils, placement);
    }

    @NotNull
    public static PlacementContribution getCuboidPlacement(IServerUtils utils, CuboidPlacement placement, ColumnContext ignoredCtx) {
        return opaqueCount(utils, placement);
    }

    @NotNull
    public static PlacementContribution getRandomChancePlacement(IServerUtils ignoredUtils, RandomChancePlacement placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofChance(NumberExpr.constant(placement.chance()));
    }

    @NotNull
    public static PlacementContribution getRandomlySelectedPlacement(IServerUtils utils, RandomlySelectedPlacement placement, ColumnContext ctx) {
        List<PlacementContribution> contributions = placement.placements().stream().map((modifier) -> utils.getPlacementContribution(utils, modifier, ctx)).toList();
        NumberExpr count = select(contributions, PlacementContribution::count);
        TooltipNode countDetails = contributions.stream().map(PlacementContribution::countDetails).filter(Objects::nonNull).findFirst().orElse(null);
        NumberExpr chance = select(contributions, PlacementContribution::chance);
        List<HeightSpan> heights = contributions.stream().map(PlacementContribution::height).filter(Objects::nonNull).toList();
        HeightSpan height = null;

        if (contributions.size() == 1 && heights.size() == 1) {
            height = heights.getFirst();
        } else if (heights.size() == contributions.size() && heights.stream().allMatch((h) -> h.height() != null)) {
            height = HeightSpan.of(NumberExpr.weighted(heights.stream().map((h) -> new NumberExpr.WeightedEntry(1, h.height())).toList()));
        } else if (!heights.isEmpty()) {
            height = HeightSpan.of(NumberExpr.opaque(typeId(placement)));
        }

        return new PlacementContribution(count, countDetails, chance, height);
    }

    @Nullable
    private static NumberExpr select(List<PlacementContribution> contributions, Function<PlacementContribution, NumberExpr> axis) {
        if (contributions.stream().map(axis).allMatch(Objects::isNull)) {
            return null;
        }

        return NumberExpr.weighted(contributions.stream().map((c) -> new NumberExpr.WeightedEntry(1, Objects.requireNonNullElse(axis.apply(c), NumberExpr.constant(1)))).toList());
    }

    @NotNull
    public static PlacementContribution getRarityFilter(IServerUtils ignoredUtils, RarityFilter placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofChance(NumberExpr.constant(1.0 / placement.chance));
    }

    @NotNull
    public static PlacementContribution getHeightRangePlacement(IServerUtils utils, HeightRangePlacement placement, ColumnContext ctx) {
        return PlacementContribution.ofHeight(HeightSpan.of(utils.convertHeightProvider(utils, placement.height, ctx)));
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
        return opaqueCount(utils, placement);
    }

    @NotNull
    private static <T extends PlacementModifier> PlacementContribution opaqueCount(IServerUtils utils, T placement) {
        return PlacementContribution.ofCount(NumberExpr.opaque(typeId(placement)), utils.getPlacementModifierTooltip(utils, placement).build());
    }

    @NotNull
    private static String typeId(PlacementModifier placement) {
        return String.valueOf(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE.getKey(placement.codec()));
    }
}
