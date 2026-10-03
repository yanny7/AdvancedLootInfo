package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.CuboidPlacement;
import net.minecraft.world.level.levelgen.placement.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class PlacementPropagatorUtils {
    @NotNull
    public static PlacementContribution getCountPlacement(IServerUtils utils, CountPlacement placement, ColumnContext ignoredCtx) {
        List<TooltipNode> conditions = new ArrayList<>();
        NumberExpr count = utils.convertIntProvider(utils, placement.count, conditions);

        return PlacementContribution.ofCount(count, conditions);
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
        return PlacementContribution.ofChance(NumberExpr.constant(placement.chance()), List.of());
    }

    @NotNull
    public static PlacementContribution getRandomlySelectedPlacement(IServerUtils utils, RandomlySelectedPlacement placement, ColumnContext ctx) {
        List<PlacementContribution> contributions = placement.placements().stream().map((modifier) -> utils.getPlacementContribution(utils, modifier, ctx)).toList();
        List<TooltipNode> countConditions = new ArrayList<>();
        NumberExpr count = select(contributions, PlacementContribution::count, PlacementContribution::countConditions, countConditions);
        TooltipNode countDetails = contributions.stream().map(PlacementContribution::countDetails).filter(Objects::nonNull).findFirst().orElse(null);
        List<TooltipNode> chanceConditions = new ArrayList<>();
        NumberExpr chance = select(contributions, PlacementContribution::chance, PlacementContribution::chanceConditions, chanceConditions);
        List<HeightSpan> heights = contributions.stream().map(PlacementContribution::height).filter(Objects::nonNull).toList();
        HeightSpan height = null;

        if (contributions.size() == 1 && heights.size() == 1) {
            height = heights.getFirst();
        } else if (heights.size() == contributions.size() && heights.stream().allMatch((h) -> h.height() != null)) {
            List<TooltipNode> heightConditions = new ArrayList<>();
            List<NumberExpr.WeightedEntry> entries = new ArrayList<>();

            for (HeightSpan h : heights) {
                entries.add(new NumberExpr.WeightedEntry(1, h.height().shiftConditions(heightConditions.size())));
                heightConditions.addAll(h.conditions());
            }

            height = HeightSpan.of(NumberExpr.weighted(entries), heightConditions);
        } else if (!heights.isEmpty()) {
            height = HeightSpan.of(NumberExpr.opaque(typeId(placement)), List.of());
        }

        return new PlacementContribution(count, countConditions, countDetails, chance, chanceConditions, height);
    }

    @Nullable
    private static NumberExpr select(List<PlacementContribution> contributions, Function<PlacementContribution, NumberExpr> axis,
                                     Function<PlacementContribution, List<TooltipNode>> axisConditions, List<TooltipNode> conditions) {
        if (contributions.stream().map(axis).allMatch(Objects::isNull)) {
            return null;
        }

        List<NumberExpr.WeightedEntry> entries = new ArrayList<>();

        for (PlacementContribution c : contributions) {
            NumberExpr value = axis.apply(c);

            if (value == null) {
                entries.add(new NumberExpr.WeightedEntry(1, NumberExpr.constant(1)));
            } else {
                entries.add(new NumberExpr.WeightedEntry(1, value.shiftConditions(conditions.size())));
                conditions.addAll(axisConditions.apply(c));
            }
        }

        return NumberExpr.weighted(entries);
    }

    @NotNull
    public static PlacementContribution getRarityFilter(IServerUtils ignoredUtils, RarityFilter placement, ColumnContext ignoredCtx) {
        return PlacementContribution.ofChance(NumberExpr.constant(1.0 / placement.chance), List.of());
    }

    @NotNull
    public static PlacementContribution getHeightRangePlacement(IServerUtils utils, HeightRangePlacement placement, ColumnContext ctx) {
        List<TooltipNode> conditions = new ArrayList<>();
        NumberExpr height = utils.convertHeightProvider(utils, placement.height, ctx, conditions);

        return PlacementContribution.ofHeight(HeightSpan.of(height, conditions));
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
