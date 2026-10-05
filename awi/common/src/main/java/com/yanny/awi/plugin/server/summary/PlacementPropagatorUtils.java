package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlacementPropagatorUtils {
    @NotNull
    public static PlacementContribution getCountPlacement(IServerUtils utils, CountPlacement placement, ColumnContext ignoredCtx) {
        List<TooltipNode> conditions = new ArrayList<>();
        NumberExpr count = utils.convertIntProvider(utils, placement.count, conditions);

        return PlacementContribution.ofCount(count, conditions);
    }

    @NotNull
    public static PlacementContribution getCountOnEveryLayerPlacement(IServerUtils utils, CountOnEveryLayerPlacement placement, ColumnContext ignoredCtx) {
        return worldDependentCount(utils, placement);
    }

    @NotNull
    public static PlacementContribution getNoiseBasedCountPlacement(IServerUtils utils, NoiseBasedCountPlacement placement, ColumnContext ignoredCtx) {
        return worldDependentCount(utils, placement);
    }

    @NotNull
    public static PlacementContribution getNoiseThresholdCountPlacement(IServerUtils utils, NoiseThresholdCountPlacement placement, ColumnContext ignoredCtx) {
        return worldDependentCount(utils, placement);
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
        return worldDependentCount(utils, placement);
    }

    @NotNull
    private static <T extends PlacementModifier> PlacementContribution worldDependentCount(IServerUtils utils, T placement) {
        NumberExpr count = NumberExpr.opaque(String.valueOf(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE.getKey(placement.type())));

        return PlacementContribution.ofCount(count, utils.getPlacementModifierTooltip(utils, placement).build());
    }
}
