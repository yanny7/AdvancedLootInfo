package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;

public class FeatureConfigurationTooltipUtils {
    @NotNull
    public static TooltipBuilder getBlockColumnConfigurationTooltip(IServerUtils utils, BlockColumnConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.layers()).build(Lang.Branch.LAYERS));
            b.add(utils.getValueTooltip(utils, configuration.direction()).build(Lang.Value.DIRECTION));
            b.add(utils.getValueTooltip(utils, configuration.allowedPlacement()).build(Lang.Branch.ALLOWED_PLACEMENT));
            b.add(utils.getValueTooltip(utils, configuration.prioritizeTip()).build(Lang.Value.PRIORITIZE_TIP));
        }, Lang.FeatureConfiguration.BLOCK_COLUMN);
    }

    @NotNull
    public static TooltipBuilder getBlockPileConfigurationTooltip(IServerUtils utils, BlockPileConfiguration configuration) {
        return array((b) -> b.add(utils.getValueTooltip(utils, configuration.stateProvider).build(Lang.Branch.STATE_PROVIDER)), Lang.FeatureConfiguration.BLOCK_PILE);
    }

    @NotNull
    public static TooltipBuilder getBlockStateConfigurationTooltip(IServerUtils utils, BlockStateConfiguration configuration) {
        return array((b) -> b.add(utils.getValueTooltip(utils, configuration.state).build(Lang.Branch.STATE)), Lang.FeatureConfiguration.BLOCK_STATE);
    }

    @NotNull
    public static TooltipBuilder getColumnFeatureConfigurationTooltip(IServerUtils utils, ColumnFeatureConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.reach()).build(Lang.Branch.REACH));
            b.add(utils.getValueTooltip(utils, configuration.height()).build(Lang.Branch.HEIGHT));
        }, Lang.FeatureConfiguration.COLUMN_FEATURE);
    }

    @NotNull
    public static TooltipBuilder getCountConfigurationTooltip(IServerUtils utils, CountConfiguration configuration) {
        return array((b) -> b.add(utils.getValueTooltip(utils, configuration.count()).build(Lang.Branch.COUNT)), Lang.FeatureConfiguration.COUNT);
    }

    @NotNull
    public static TooltipBuilder getDeltaFeatureConfigurationTooltip(IServerUtils utils, DeltaFeatureConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.contents()).build(Lang.Branch.CONTENTS));
            b.add(utils.getValueTooltip(utils, configuration.rim()).build(Lang.Branch.RIM));
            b.add(utils.getValueTooltip(utils, configuration.size()).build(Lang.Branch.SIZE));
            b.add(utils.getValueTooltip(utils, configuration.rimSize()).build(Lang.Branch.RIM_SIZE));
        }, Lang.FeatureConfiguration.DELTA_FEATURE);
    }

    @NotNull
    public static TooltipBuilder getDiscConfigurationTooltip(IServerUtils utils, DiskConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.stateProvider()).build(Lang.Branch.STATE_PROVIDER));
            b.add(utils.getValueTooltip(utils, configuration.target()).build(Lang.Branch.TARGET));
            b.add(utils.getValueTooltip(utils, configuration.radius()).build(Lang.Branch.RADIUS));
            b.add(utils.getValueTooltip(utils, configuration.halfHeight()).build(Lang.Value.HALF_HEIGHT));
        }, Lang.FeatureConfiguration.DISK);
    }

    @NotNull
    public static TooltipBuilder getDripstoneClusterConfigurationTooltip(IServerUtils utils, DripstoneClusterConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.floorToCeilingSearchRange).build(Lang.Value.SEARCH_RANGE));
            b.add(utils.getValueTooltip(utils, configuration.height).build(Lang.Branch.HEIGHT));
            b.add(utils.getValueTooltip(utils, configuration.radius).build(Lang.Branch.RADIUS));
            b.add(utils.getValueTooltip(utils, configuration.maxStalagmiteStalactiteHeightDiff).build(Lang.Value.MAX_HEIGHT_DIFF));
            b.add(utils.getValueTooltip(utils, configuration.heightDeviation).build(Lang.Value.HEIGHT_DEVIATION));
            b.add(utils.getValueTooltip(utils, configuration.dripstoneBlockLayerThickness).build(Lang.Branch.LAYER_THICKNESS));
            b.add(utils.getValueTooltip(utils, configuration.density).build(Lang.Branch.DENSITY));
            b.add(utils.getValueTooltip(utils, configuration.wetness).build(Lang.Branch.WETNESS));
            b.add(utils.getValueTooltip(utils, configuration.chanceOfDripstoneColumnAtMaxDistanceFromCenter).build(Lang.Value.EDGE_CHANCE));
            b.add(utils.getValueTooltip(utils, configuration.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn).build(Lang.Value.CHANCE_RADIUS));
            b.add(utils.getValueTooltip(utils, configuration.maxDistanceFromCenterAffectingHeightBias).build(Lang.Value.HEIGHT_BIAS_RADIUS));
        }, Lang.FeatureConfiguration.DRIPSTONE_CLUSTER);
    }

    @NotNull
    public static TooltipBuilder getEndGatewayConfigurationTooltip(IServerUtils utils, EndGatewayConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.getExit()).build(Lang.Value.EXIT));
            b.add(utils.getValueTooltip(utils, configuration.isExitExact()).build(Lang.Value.EXACT));
        }, Lang.FeatureConfiguration.END_GATEWAY);
    }

    @NotNull
    public static TooltipBuilder getGeodeConfigurationTooltip(IServerUtils utils, GeodeConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.geodeBlockSettings).build(Lang.Branch.GEODE_BLOCK_SETTINGS));
            b.add(utils.getValueTooltip(utils, configuration.geodeLayerSettings).build(Lang.Branch.GEODE_LAYER_SETTINGS));
            b.add(utils.getValueTooltip(utils, configuration.geodeCrackSettings).build(Lang.Branch.GEODE_CRACK_SETTINGS));
            b.add(utils.getValueTooltip(utils, configuration.usePotentialPlacementsChance).build(Lang.Value.POTENTIAL_PLACEMENT_CHANCE));
            b.add(utils.getValueTooltip(utils, configuration.useAlternateLayer0Chance).build(Lang.Value.ALTERNATE_LAYER_CHANCE));
            b.add(utils.getValueTooltip(utils, configuration.placementsRequireLayer0Alternate).build(Lang.Value.REQUIRE_ALTERNATE_LAYER));
            b.add(utils.getValueTooltip(utils, configuration.outerWallDistance).build(Lang.Branch.OUTER_WALL_DISTANCE));
            b.add(utils.getValueTooltip(utils, configuration.distributionPoints).build(Lang.Branch.DISTRIBUTION_POINTS));
            b.add(utils.getValueTooltip(utils, configuration.pointOffset).build(Lang.Branch.POINT_OFFSET));
            b.add(utils.getValueTooltip(utils, configuration.minGenOffset).build(Lang.Value.MIN_GEN_OFFSET));
            b.add(utils.getValueTooltip(utils, configuration.maxGenOffset).build(Lang.Value.MAX_GEN_OFFSET));
            b.add(utils.getValueTooltip(utils, configuration.noiseMultiplier).build(Lang.Value.NOISE_MULTIPLIER));
            b.add(utils.getValueTooltip(utils, configuration.invalidBlocksThreshold).build(Lang.Value.INVALID_BLOCKS_THRESHOLD));
        }, Lang.FeatureConfiguration.GEODE);
    }

    @NotNull
    public static TooltipBuilder getHugeMushroomFeatureConfigurationTooltip(IServerUtils utils, HugeMushroomFeatureConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.capProvider).build(Lang.Branch.CAP_PROVIDER));
            b.add(utils.getValueTooltip(utils, configuration.stemProvider).build(Lang.Branch.STEM_PROVIDER));
            b.add(utils.getValueTooltip(utils, configuration.foliageRadius).build(Lang.Value.FOLIAGE_RADIUS));
        }, Lang.FeatureConfiguration.HUGE_MUSHROOM_FEATURE);
    }

    @NotNull
    public static TooltipBuilder getLargeDripstoneConfigurationTooltip(IServerUtils utils, LargeDripstoneConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.floorToCeilingSearchRange).build(Lang.Value.SEARCH_RANGE));
            b.add(utils.getValueTooltip(utils, configuration.columnRadius).build(Lang.Branch.COLUMN_RADIUS));
            b.add(utils.getValueTooltip(utils, configuration.heightScale).build(Lang.Branch.HEIGHT_SCALE));
            b.add(utils.getValueTooltip(utils, configuration.maxColumnRadiusToCaveHeightRatio).build(Lang.Value.RADIUS_TO_HEIGHT_RATIO));
            b.add(utils.getValueTooltip(utils, configuration.stalactiteBluntness).build(Lang.Branch.STALACTITE_BLUNTNESS));
            b.add(utils.getValueTooltip(utils, configuration.stalagmiteBluntness).build(Lang.Branch.STALAGMITE_BLUNTNESS));
            b.add(utils.getValueTooltip(utils, configuration.windSpeed).build(Lang.Branch.WIND_SPEED));
            b.add(utils.getValueTooltip(utils, configuration.minRadiusForWind).build(Lang.Value.MIN_RADIUS_FOR_WIND));
            b.add(utils.getValueTooltip(utils, configuration.minBluntnessForWind).build(Lang.Value.MIN_BLUNTNESS_FOR_WIND));
        }, Lang.FeatureConfiguration.LARGE_DRIPSTONE);
    }

    @NotNull
    public static TooltipBuilder getLayeredConfigurationTooltip(IServerUtils utils, LayerConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.height).build(Lang.Branch.HEIGHT));
            b.add(utils.getValueTooltip(utils, configuration.state).build(Lang.Branch.STATE));
        }, Lang.FeatureConfiguration.LAYERED);
    }

    @NotNull
    public static TooltipBuilder getMultifaceGrowthConfigurationTooltip(IServerUtils utils, MultifaceGrowthConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.placeBlock).build(Lang.Value.PLACE_BLOCK));
            b.add(utils.getValueTooltip(utils, configuration.searchRange).build(Lang.Value.SEARCH_RANGE));
            b.add(utils.getValueTooltip(utils, configuration.canPlaceOnFloor).build(Lang.Value.CAN_PLACE_ON_FLOOR));
            b.add(utils.getValueTooltip(utils, configuration.canPlaceOnCeiling).build(Lang.Value.CAN_PLACE_ON_CEILING));
            b.add(utils.getValueTooltip(utils, configuration.canPlaceOnWall).build(Lang.Value.CAN_PLACE_ON_WALL));
            b.add(utils.getValueTooltip(utils, configuration.chanceOfSpreading).build(Lang.Value.CHANCE_OF_SPREADING));
            b.add(utils.getValueTooltip(utils, configuration.canBePlacedOn).build(Lang.Branch.CAN_BE_PLACED_ON));
        }, Lang.FeatureConfiguration.MULTIFACE_GROWTH);
    }

    @NotNull
    public static TooltipBuilder getNetherForestVegetationConfigurationTooltip(IServerUtils utils, NetherForestVegetationConfig configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.spreadWidth).build(Lang.Value.SPREAD_WIDTH));
            b.add(utils.getValueTooltip(utils, configuration.spreadHeight).build(Lang.Value.SPREAD_HEIGHT));
        }, Lang.FeatureConfiguration.NETHER_FOREST_VEGETATION);
    }

    @NotNull
    public static TooltipBuilder getNoneFeatureConfigurationTooltip(IServerUtils ignoredUtils, NoneFeatureConfiguration ignoredConfiguration) {
        return array(TooltipBuilder::showEmpty, Lang.FeatureConfiguration.NONE_FEATURE);
    }

    @NotNull
    public static TooltipBuilder getOreConfigurationTooltip(IServerUtils utils, OreConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.discardChanceOnAirExposure).build(Lang.Value.DISCARD_CHANCE_ON_AIR_EXPOSURE));
            b.add(utils.getValueTooltip(utils, configuration.size).build(Lang.Value.SIZE));
            b.add(utils.getValueTooltip(utils, configuration.targetStates).build(Lang.Branch.TARGET_STATES));
        }, Lang.FeatureConfiguration.ORE);
    }

    @NotNull
    public static TooltipBuilder getPointedDripstoneConfigurationTooltip(IServerUtils utils, PointedDripstoneConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.chanceOfTallerDripstone).build(Lang.Value.CHANCE_OF_TALLER_DRIPSTONE));
            b.add(utils.getValueTooltip(utils, configuration.chanceOfDirectionalSpread).build(Lang.Value.CHANCE_OF_DIRECTIONAL_SPREAD));
            b.add(utils.getValueTooltip(utils, configuration.chanceOfSpreadRadius2).build(Lang.Value.CHANCE_OF_SPREAD_RADIUS_2));
            b.add(utils.getValueTooltip(utils, configuration.chanceOfSpreadRadius3).build(Lang.Value.CHANCE_OF_SPREAD_RADIUS_3));
        }, Lang.FeatureConfiguration.POINTED_DRIPSTONE);
    }

    @NotNull
    public static TooltipBuilder getProbabilityFeatureConfigurationTooltip(IServerUtils utils, ProbabilityFeatureConfiguration configuration) {
        return array((b) -> b.add(utils.getValueTooltip(utils, configuration.probability).build(Lang.Value.PROBABILITY)), Lang.FeatureConfiguration.PROBABILITY_FEATURE);
    }

    @NotNull
    public static TooltipBuilder getRandomBooleanFeatureConfigurationTooltip(IServerUtils utils, RandomBooleanFeatureConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.featureTrue).build(Lang.Branch.FEATURE_TRUE));
            b.add(utils.getValueTooltip(utils, configuration.featureFalse).build(Lang.Branch.FEATURE_FALSE));
        }, Lang.FeatureConfiguration.RANDOM_BOOLEAN_FEATURE);
    }

    @NotNull
    public static TooltipBuilder getRandomFeatureConfigurationTooltip(IServerUtils utils, RandomFeatureConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.features).build(Lang.Branch.FEATURES));
            b.add(utils.getValueTooltip(utils, configuration.defaultFeature).build(Lang.Branch.DEFAULT_FEATURE));
        }, Lang.FeatureConfiguration.RANDOM_FEATURE);
    }

    @NotNull
    public static TooltipBuilder getRandomPatchConfigurationTooltip(IServerUtils utils, RandomPatchConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.tries()).build(Lang.Value.TRIES));
            b.add(utils.getValueTooltip(utils, configuration.xzSpread()).build(Lang.Value.XZ_SPREAD));
            b.add(utils.getValueTooltip(utils, configuration.ySpread()).build(Lang.Value.Y_SPREAD));
            b.add(utils.getValueTooltip(utils, configuration.feature()).build(Lang.Branch.FEATURE));
        }, Lang.FeatureConfiguration.RANDOM_PATCH);
    }

    @NotNull
    public static TooltipBuilder getReplaceableBlockConfigurationTooltip(IServerUtils utils, ReplaceBlockConfiguration configuration) {
        return array((b) -> b.add(utils.getValueTooltip(utils, configuration.targetStates).build(Lang.Branch.TARGET_STATES)), Lang.FeatureConfiguration.REPLACEABLE_BLOCK);
    }

    @NotNull
    public static TooltipBuilder getReplaceableSphereConfigurationTooltip(IServerUtils utils, ReplaceSphereConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.targetState).build(Lang.Branch.TARGET_STATE));
            b.add(utils.getValueTooltip(utils, configuration.replaceState).build(Lang.Branch.REPLACE_STATE));
            b.add(utils.getValueTooltip(utils, configuration.radius()).build(Lang.Branch.RADIUS));
        }, Lang.FeatureConfiguration.REPLACEABLE_SPHERE);
    }

    @NotNull
    public static TooltipBuilder getRootSystemConfigurationTooltip(IServerUtils utils, RootSystemConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.treeFeature).build(Lang.Branch.TREE_FEATURE));
            b.add(utils.getValueTooltip(utils, configuration.requiredVerticalSpaceForTree).build(Lang.Value.REQUIRED_VERTICAL_SPACE_FOR_TREE));
            b.add(utils.getValueTooltip(utils, configuration.rootRadius).build(Lang.Value.ROOT_RADIUS));
            b.add(utils.getValueTooltip(utils, configuration.rootReplaceable).build(Lang.Value.ROOT_REPLACEABLE));
            b.add(utils.getValueTooltip(utils, configuration.rootStateProvider).build(Lang.Branch.ROOT_STATE_PROVIDER));
            b.add(utils.getValueTooltip(utils, configuration.rootPlacementAttempts).build(Lang.Value.ROOT_PLACEMENT_ATTEMPTS));
            b.add(utils.getValueTooltip(utils, configuration.rootColumnMaxHeight).build(Lang.Value.ROOT_COLUMN_MAX_HEIGHT));
            b.add(utils.getValueTooltip(utils, configuration.hangingRootRadius).build(Lang.Value.HANGING_ROOT_RADIUS));
            b.add(utils.getValueTooltip(utils, configuration.hangingRootsVerticalSpan).build(Lang.Value.HANGING_ROOT_VERTICAL_SPAN));
            b.add(utils.getValueTooltip(utils, configuration.hangingRootStateProvider).build(Lang.Branch.HANGING_ROOT_STATE_PROVIDER));
            b.add(utils.getValueTooltip(utils, configuration.hangingRootPlacementAttempts).build(Lang.Value.HANGING_ROOT_PLACEMENT_ATTEMPTS));
            b.add(utils.getValueTooltip(utils, configuration.allowedVerticalWaterForTree).build(Lang.Value.ALLOWED_VERTICAL_WATER_FOR_TREE));
            b.add(utils.getValueTooltip(utils, configuration.allowedTreePosition).build(Lang.Branch.ALLOWED_TREE_POSITION));
        }, Lang.FeatureConfiguration.ROOT_SYSTEM);
    }

    @NotNull
    public static TooltipBuilder getSculkPatchConfigurationTooltip(IServerUtils utils, SculkPatchConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.chargeCount()).build(Lang.Value.CHARGE_COUNT));
            b.add(utils.getValueTooltip(utils, configuration.amountPerCharge()).build(Lang.Value.AMOUNT_PER_CHARGE));
            b.add(utils.getValueTooltip(utils, configuration.spreadAttempts()).build(Lang.Value.SPREAD_ATTEMPTS));
            b.add(utils.getValueTooltip(utils, configuration.growthRounds()).build(Lang.Value.GROWTH_ROUNDS));
            b.add(utils.getValueTooltip(utils, configuration.spreadRounds()).build(Lang.Value.SPREAD_ROUNDS));
            b.add(utils.getValueTooltip(utils, configuration.extraRareGrowths()).build(Lang.Branch.EXTRA_RARE_GROWTHS));
            b.add(utils.getValueTooltip(utils, configuration.catalystChance()).build(Lang.Value.CATALYST_CHANCE));
        }, Lang.FeatureConfiguration.SCULK_PATCH);
    }

    @NotNull
    public static TooltipBuilder getSimpleBlockConfigurationTooltip(IServerUtils utils, SimpleBlockConfiguration configuration) {
        return array((b) -> b.add(utils.getValueTooltip(utils, configuration.toPlace()).build(Lang.Branch.TO_PLACE)), Lang.FeatureConfiguration.SIMPLE_BLOCK);
    }

    @NotNull
    public static TooltipBuilder getSimpleRandomFeatureConfigurationTooltip(IServerUtils utils, SimpleRandomFeatureConfiguration configuration) {
        return array((b) -> b.add(utils.getValueTooltip(utils, configuration.features).build(Lang.Branch.FEATURES)), Lang.FeatureConfiguration.SIMPLE_RANDOM_FEATURES);
    }

    @NotNull
    public static TooltipBuilder getSpikeConfigurationTooltip(IServerUtils utils, SpikeConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.isCrystalInvulnerable()).build(Lang.Value.IS_CRYSTAL_VULNERABLE));
            b.add(utils.getValueTooltip(utils, configuration.getSpikes()).build(Lang.Branch.SPIKES));
            b.add(utils.getValueTooltip(utils, configuration.getCrystalBeamTarget()).build(Lang.Value.CRYSTAL_BEAM_TARGET));
        }, Lang.FeatureConfiguration.SPIKE);
    }

    @NotNull
    public static TooltipBuilder getSpringConfigurationTooltip(IServerUtils utils, SpringConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.state).build(Lang.Branch.STATE));
            b.add(utils.getValueTooltip(utils, configuration.requiresBlockBelow).build(Lang.Value.REQUIRES_BLOCK_BELOW));
            b.add(utils.getValueTooltip(utils, configuration.rockCount).build(Lang.Value.ROCK_COUNT));
            b.add(utils.getValueTooltip(utils, configuration.holeCount).build(Lang.Value.HOLE_COUNT));
            b.add(utils.getValueTooltip(utils, configuration.validBlocks).build(Lang.Branch.VALID_BLOCKS));
        }, Lang.FeatureConfiguration.SPRING);
    }

    @NotNull
    public static TooltipBuilder getTreeConfigurationTooltip(IServerUtils utils, TreeConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.trunkProvider).build(Lang.Branch.TRUNK_PROVIDER));
            b.add(utils.getValueTooltip(utils, configuration.dirtProvider).build(Lang.Branch.DIRT_PROVIDER));
            b.add(utils.getValueTooltip(utils, configuration.trunkPlacer).build(Lang.Branch.TRUNK_PLACER));
            b.add(utils.getValueTooltip(utils, configuration.foliageProvider).build(Lang.Branch.FOLIAGE_PROVIDER));
            b.add(utils.getValueTooltip(utils, configuration.foliagePlacer).build(Lang.Branch.FOLIAGE_PLACER));
            b.add(utils.getValueTooltip(utils, configuration.rootPlacer).build(Lang.Branch.ROOT_PLACER));
            b.add(utils.getValueTooltip(utils, configuration.minimumSize).build(Lang.Branch.MINIMUM_SIZE));
            b.add(utils.getValueTooltip(utils, configuration.decorators).build(Lang.Branch.DECORATORS));
            b.add(utils.getValueTooltip(utils, configuration.ignoreVines).build(Lang.Value.IGNORE_VINES));
            b.add(utils.getValueTooltip(utils, configuration.forceDirt).build(Lang.Value.FORCE_DIRT));
        }, Lang.FeatureConfiguration.TREE);
    }

    @NotNull
    public static TooltipBuilder getTwistingVinesConfigurationTooltip(IServerUtils utils, TwistingVinesConfig configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.spreadWidth()).build(Lang.Value.SPREAD_WIDTH));
            b.add(utils.getValueTooltip(utils, configuration.spreadHeight()).build(Lang.Value.SPREAD_HEIGHT));
            b.add(utils.getValueTooltip(utils, configuration.maxHeight()).build(Lang.Value.MAX_HEIGHT));
        }, Lang.FeatureConfiguration.TWISTING_VINES);
    }

    @NotNull
    public static TooltipBuilder getUnderwaterMagmaConfigurationTooltip(IServerUtils utils, UnderwaterMagmaConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.floorSearchRange).build(Lang.Value.FLOOR_RANGE_SEARCH));
            b.add(utils.getValueTooltip(utils, configuration.placementRadiusAroundFloor).build(Lang.Value.PLACEMENT_RADIUS_AROUND_FLOOR));
            b.add(utils.getValueTooltip(utils, configuration.placementProbabilityPerValidPosition).build(Lang.Value.PROBABILITY_PER_POSITION));
        }, Lang.FeatureConfiguration.UNDERWATER_MAGMA);
    }

    @NotNull
    public static TooltipBuilder getVegetationPatchConfigurationTooltip(IServerUtils utils, VegetationPatchConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.replaceable).build(Lang.Value.REPLACEABLE));
            b.add(utils.getValueTooltip(utils, configuration.groundState).build(Lang.Branch.GROUND_STATE));
            b.add(utils.getValueTooltip(utils, configuration.vegetationFeature).build(Lang.Branch.VEGETATION_FEATURE));
            b.add(utils.getValueTooltip(utils, configuration.surface).build(Lang.Value.SURFACE));
            b.add(utils.getValueTooltip(utils, configuration.depth).build(Lang.Branch.DEPTH));
            b.add(utils.getValueTooltip(utils, configuration.extraBottomBlockChance).build(Lang.Value.EXTRA_BOTTOM_BLOCK_CHANCE));
            b.add(utils.getValueTooltip(utils, configuration.verticalRange).build(Lang.Value.VERTICAL_RANGE));
            b.add(utils.getValueTooltip(utils, configuration.vegetationChance).build(Lang.Value.VEGETATION_CHANCE));
            b.add(utils.getValueTooltip(utils, configuration.xzRadius).build(Lang.Branch.XZ_RADIUS));
            b.add(utils.getValueTooltip(utils, configuration.extraEdgeColumnChance).build(Lang.Value.EXTRA_EDGE_COLUMN_CHANCE));
        }, Lang.FeatureConfiguration.VEGETATION_PATCH);
    }

    @NotNull
    public static TooltipBuilder getLakeConfigurationTooltip(IServerUtils utils, LakeFeature.Configuration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.fluid()).build(Lang.Branch.FLUID));
            b.add(utils.getValueTooltip(utils, configuration.barrier()).build(Lang.Branch.BARRIER));
        }, Lang.FeatureConfiguration.LAKE);
    }

    @NotNull
    public static TooltipBuilder getFossilFeatureConfigurationTooltip(IServerUtils utils, FossilFeatureConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.fossilStructures).build(Lang.Branch.FOSSIL_STRUCTURES));
            b.add(utils.getValueTooltip(utils, configuration.overlayStructures).build(Lang.Branch.OVERLAY_STRUCTURES));
            b.add(utils.getValueTooltip(utils, configuration.fossilProcessors).build(Lang.Branch.FOSSIL_PROCESSORS));
            b.add(utils.getValueTooltip(utils, configuration.overlayProcessors).build(Lang.Branch.OVERLAY_PROCESSORS));
            b.add(utils.getValueTooltip(utils, configuration.maxEmptyCornersAllowed).build(Lang.Value.MAX_EMPTY_CORNERS_ALLOWED));
        }, Lang.FeatureConfiguration.FOSSIL_FEATURE);
    }

    @NotNull
    public static TooltipBuilder getHugeFungusConfigurationTooltip(IServerUtils utils, HugeFungusConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.validBaseState).build(Lang.Branch.VALID_BASE_STATE));
            b.add(utils.getValueTooltip(utils, configuration.stemState).build(Lang.Branch.STEM_STATE));
            b.add(utils.getValueTooltip(utils, configuration.hatState).build(Lang.Branch.HAT_STATE));
            b.add(utils.getValueTooltip(utils, configuration.decorState).build(Lang.Branch.DECOR_STATE));
            b.add(utils.getValueTooltip(utils, configuration.replaceableBlocks).build(Lang.Branch.REPLACEABLE_BLOCKS));
            b.add(utils.getValueTooltip(utils, configuration.planted).build(Lang.Value.PLANTED));
        }, Lang.FeatureConfiguration.HUGE_FUNGUS);
    }
}
