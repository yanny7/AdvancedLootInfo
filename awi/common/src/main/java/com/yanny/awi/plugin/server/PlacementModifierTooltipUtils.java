package com.yanny.awi.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.placement.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;
import static com.yanny.aci.tooltip.TooltipBuilder.empty;

public class PlacementModifierTooltipUtils {
    @NotNull
    public static TooltipBuilder getBiomeFilterTooltip(IServerUtils ignoredUtils, BiomeFilter ignoredPlacement) {
        return empty(); // Nothing useful
    }

    @NotNull
    public static TooltipBuilder getBlockPredicateFilterTooltip(IServerUtils utils, BlockPredicateFilter placement) {
        return array((b) -> b.add(utils.getValueTooltip(utils, placement.predicate)), Lang.PlacementModifier.BLOCK_PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getCountOnEveryLayerPlacementTooltip(IServerUtils utils, CountOnEveryLayerPlacement placement) {
        return array((b) -> b.add(utils.getValueTooltip(utils, placement.count).build(Lang.Branch.COUNT)), Lang.PlacementModifier.COUNT_ON_EVERY_LAYER);
    }

    @NotNull
    public static TooltipBuilder getCountPlacementTooltip(IServerUtils utils, CountPlacement placement) {
        return array((b) -> b.add(utils.getValueTooltip(utils, placement.count).build(Lang.Branch.COUNT)), Lang.PlacementModifier.COUNT_PLACEMENT);
    }

    @NotNull
    public static TooltipBuilder getEnvironmentScanPlacementTooltip(IServerUtils utils, EnvironmentScanPlacement placement) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, placement.directionOfSearch).build(Lang.Value.DIRECTION_OF_SEARCH));
            b.add(utils.getValueTooltip(utils, placement.targetCondition).build(Lang.Branch.TARGET_CONDITION));
            b.add(utils.getValueTooltip(utils, placement.allowedSearchCondition).build(Lang.Branch.ALLOWED_SEARCH_CONDITION));
            b.add(utils.getValueTooltip(utils, placement.maxSteps).build(Lang.Value.MAX_STEPS));
        }, Lang.PlacementModifier.ENVIRONMENT_SCAN_PLACEMENT);
    }

    @NotNull
    public static TooltipBuilder getHeightmapPlacementTooltip(IServerUtils utils, HeightmapPlacement placement) {
        return array((b) -> b.add(utils.getValueTooltip(utils, placement.heightmap).build(Lang.Value.HEIGHTMAP)), Lang.PlacementModifier.HEIGHTMAP_PLACEMENT);
    }

    @NotNull
    public static TooltipBuilder getHeightRangePlacementTooltip(IServerUtils utils, HeightRangePlacement placement) {
        return array((b) -> b.add(utils.getValueTooltip(utils, placement.height)), Lang.PlacementModifier.HEIGHT_RANGE_PLACEMENT);
    }

    @NotNull
    public static TooltipBuilder getInSquarePlacementTooltip(IServerUtils ignoredUtils, InSquarePlacement ignoredPlacement) {
        return empty(); // Nothing useful
    }

    @NotNull
    public static TooltipBuilder getNoiseBasedCountPlacementTooltip(IServerUtils utils, NoiseBasedCountPlacement placement) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, placement.noiseToCountRatio).build(Lang.Value.NOISE_TO_COUNT_RATIO));
            b.add(utils.getValueTooltip(utils, placement.noiseFactor).build(Lang.Value.NOISE_FACTOR));
            b.add(utils.getValueTooltip(utils, placement.noiseOffset).build(Lang.Value.NOISE_OFFSET));
        }, Lang.PlacementModifier.NOISE_BASED_COUNT_PLACEMENT);
    }

    @NotNull
    public static TooltipBuilder getNoiseThresholdCountPlacementTooltip(IServerUtils utils, NoiseThresholdCountPlacement placement) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, placement.noiseLevel).build(Lang.Value.NOISE_LEVEL));
            b.add(utils.getValueTooltip(utils, placement.belowNoise).build(Lang.Value.BELOW_NOISE));
            b.add(utils.getValueTooltip(utils, placement.aboveNoise).build(Lang.Value.ABOVE_NOISE));
        }, Lang.PlacementModifier.NOISE_THRESHOLD_COUNT_PLACEMENT);
    }

    @NotNull
    public static TooltipBuilder getRarityFilterTooltip(IServerUtils utils, RarityFilter placement) {
        return array((b) -> b.add(utils.getValueTooltip(utils, placement.chance).build(Lang.Value.CHANCE)), Lang.PlacementModifier.RARITY_FILTER);
    }

    @NotNull
    public static TooltipBuilder getRandomOffsetPlacementTooltip(IServerUtils ignoredUtils, RandomOffsetPlacement ignoredPlacement) {
        return empty(); // Nothing useful
    }

    @NotNull
    public static TooltipBuilder getSurfaceRelativeThresholdFilterTooltip(IServerUtils utils, SurfaceRelativeThresholdFilter placement) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, placement.heightmap).build(Lang.Value.HEIGHTMAP));

            if (placement.minInclusive == Integer.MIN_VALUE) {
                b.add(utils.getValueTooltip(utils, "≤" + placement.maxInclusive).build(Lang.Value.RANGE));
            } else if (placement.maxInclusive == Integer.MAX_VALUE) {
                b.add(utils.getValueTooltip(utils, "≥" + placement.minInclusive).build(Lang.Value.RANGE));
            } else {
                b.add(utils.getValueTooltip(utils, new RangeValue(placement.minInclusive, placement.maxInclusive)).build(Lang.Value.RANGE));
            }

        }, Lang.PlacementModifier.SURFACE_RELATIVE_THRESHOLD_FILTER);
    }

    @NotNull
    public static TooltipBuilder getSurfaceWaterDepthFilterTooltip(IServerUtils utils, SurfaceWaterDepthFilter placement) {
        return array((b) -> b.add(utils.getValueTooltip(utils, placement.maxWaterDepth).build(Lang.Value.MAX_WATER_DEPTH)), Lang.PlacementModifier.SURFACE_WATER_DEPTH_FILTER);
    }

    @NotNull
    public static TooltipBuilder getFixedPlacementTooltip(IServerUtils utils, FixedPlacement placement) {
        return array((b) -> b.add(utils.getValueTooltip(utils, placement.positions).build(Lang.Branch.POSITIONS)), Lang.PlacementModifier.SURFACE_WATER_DEPTH_FILTER);
    }
}
