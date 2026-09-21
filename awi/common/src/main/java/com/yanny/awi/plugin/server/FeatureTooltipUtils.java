package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.feature.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;

public class FeatureTooltipUtils {
    @NotNull
    public static TooltipBuilder getBambooTooltip(IServerUtils utils, BambooFeature feature) {
        return array((b) -> b.add(utils.getValueTooltip(utils, feature.probability()).build(Lang.Value.PROBABILITY)), Lang.Feature.BAMBOO);
    }

    @NotNull
    public static TooltipBuilder getBlockBlobTooltip(IServerUtils utils, BlockBlobFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.state()).build(Lang.Branch.STATE));
            b.add(utils.getValueTooltip(utils, feature.canPlaceOn()).build(Lang.Branch.CAN_PLACE_ON));
        }, Lang.Feature.BLOCK_BLOB);
    }

    @NotNull
    public static TooltipBuilder getBlockColumnTooltip(IServerUtils utils, BlockColumnFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.layers()).build(Lang.Branch.LAYERS));
            b.add(utils.getValueTooltip(utils, feature.direction()).build(Lang.Value.DIRECTION));
            b.add(utils.getValueTooltip(utils, feature.allowedPlacement()).build(Lang.Branch.ALLOWED_PLACEMENT));
            b.add(utils.getValueTooltip(utils, feature.prioritizeTip()).build(Lang.Value.PRIORITIZE_TIP));
        }, Lang.Feature.BLOCK_COLUMN);
    }

    @NotNull
    public static TooltipBuilder getBlockPileTooltip(IServerUtils utils, BlockPileFeature feature) {
        return array((b) -> b.add(utils.getValueTooltip(utils, feature.stateProvider()).build(Lang.Branch.STATE_PROVIDER)), Lang.Feature.BLOCK_PILE);
    }

    @NotNull
    public static TooltipBuilder getBlueIceTooltip(IServerUtils ignoredUtils, BlueIceFeature ignoredFeature) {
        return array(TooltipBuilder::showEmpty, Lang.Feature.BLUE_ICE);
    }

    @NotNull
    public static TooltipBuilder getBonusChestTooltip(IServerUtils ignoredUtils, BonusChestFeature ignoredFeature) {
        return array(TooltipBuilder::showEmpty, Lang.Feature.BONUS_CHEST);
    }

    @NotNull
    public static TooltipBuilder getChorusPlantTooltip(IServerUtils ignoredUtils, ChorusPlantFeature ignoredFeature) {
        return array(TooltipBuilder::showEmpty, Lang.Feature.CHORUS_PLANT);
    }

    @NotNull
    public static TooltipBuilder getCoralClawTooltip(IServerUtils utils, CoralClawFeature feature) {
        return array((b) -> b.add(utils.getValueTooltip(utils, feature.feature()).build(Lang.Branch.FEATURE)), Lang.Feature.CORAL_CLAW);
    }

    @NotNull
    public static TooltipBuilder getCoralTreeTooltip(IServerUtils utils, CoralTreeFeature feature) {
        return array((b) -> b.add(utils.getValueTooltip(utils, feature.feature()).build(Lang.Branch.FEATURE)), Lang.Feature.CORAL_TREE);
    }

    @NotNull
    public static TooltipBuilder getDeltaTooltip(IServerUtils utils, DeltaFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.contents()).build(Lang.Branch.CONTENTS));
            b.add(utils.getValueTooltip(utils, feature.rim()).build(Lang.Branch.RIM));
            b.add(utils.getValueTooltip(utils, feature.size()).build(Lang.Branch.SIZE));
            b.add(utils.getValueTooltip(utils, feature.rimSize()).build(Lang.Branch.RIM_SIZE));
        }, Lang.Feature.DELTA_FEATURE);
    }

    @NotNull
    public static TooltipBuilder getDiskTooltip(IServerUtils utils, DiskFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.stateProvider()).build(Lang.Branch.STATE_PROVIDER));
            b.add(utils.getValueTooltip(utils, feature.target()).build(Lang.Branch.TARGET));
            b.add(utils.getValueTooltip(utils, feature.radius()).build(Lang.Branch.RADIUS));
            b.add(utils.getValueTooltip(utils, feature.halfHeight()).build(Lang.Value.HALF_HEIGHT));
        }, Lang.Feature.DISK);
    }

    @NotNull
    public static TooltipBuilder getEndGatewayTooltip(IServerUtils utils, EndGatewayFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.exit()).build(Lang.Value.EXIT));
            b.add(utils.getValueTooltip(utils, feature.exact()).build(Lang.Value.EXACT));
        }, Lang.Feature.END_GATEWAY);
    }

    @NotNull
    public static TooltipBuilder getEndIslandTooltip(IServerUtils ignoredUtils, EndIslandFeature ignoredFeature) {
        return array(TooltipBuilder::showEmpty, Lang.Feature.END_ISLAND);
    }

    @NotNull
    public static TooltipBuilder getEndPlatformTooltip(IServerUtils ignoredUtils, EndPlatformFeature ignoredFeature) {
        return array(TooltipBuilder::showEmpty, Lang.Feature.END_PLATFORM);
    }

    @NotNull
    public static TooltipBuilder getEndPodiumTooltip(IServerUtils utils, EndPodiumFeature feature) {
        return array((b) -> b.add(utils.getValueTooltip(utils, feature.active()).build(Lang.Value.ACTIVE)), Lang.Feature.END_PODIUM);
    }

    @NotNull
    public static TooltipBuilder getEndSpikeTooltip(IServerUtils utils, EndSpikeFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.spikes()).build(Lang.Branch.SPIKES));
            b.add(utils.getValueTooltip(utils, feature.crystalInvulnerable()).build(Lang.Value.IS_CRYSTAL_INVULNERABLE));
            b.add(utils.getValueTooltip(utils, feature.crystalBeamTarget()).build(Lang.Value.CRYSTAL_BEAM_TARGET));
        }, Lang.Feature.END_SPIKE);
    }

    @NotNull
    public static TooltipBuilder getFallenTreeTooltip(IServerUtils utils, FallenTreeFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.trunkProvider()).build(Lang.Branch.TRUNK_PROVIDER));
            b.add(utils.getValueTooltip(utils, feature.logLength()).build(Lang.Branch.LOG_LENGTH));
            b.add(utils.getValueTooltip(utils, feature.stumpDecorators()).build(Lang.Branch.STUMP_DECORATORS));
            b.add(utils.getValueTooltip(utils, feature.logDecorators()).build(Lang.Branch.LOG_DECORATORS));
        }, Lang.Feature.FALLEN_TREE);
    }

    @NotNull
    public static TooltipBuilder getFillLayerTooltip(IServerUtils utils, FillLayerFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.height()).build(Lang.Value.HEIGHT));
            b.add(utils.getValueTooltip(utils, feature.state()).build(Lang.Branch.STATE));
        }, Lang.Feature.FILL_LAYER);
    }

    @NotNull
    public static TooltipBuilder getFossilTooltip(IServerUtils utils, FossilFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.fossilStructures()).build(Lang.Branch.FOSSIL_STRUCTURES));
            b.add(utils.getValueTooltip(utils, feature.overlayStructures()).build(Lang.Branch.OVERLAY_STRUCTURES));
            b.add(utils.getValueTooltip(utils, feature.fossilProcessors()).build(Lang.Branch.FOSSIL_PROCESSORS));
            b.add(utils.getValueTooltip(utils, feature.overlayProcessors()).build(Lang.Branch.OVERLAY_PROCESSORS));
            b.add(utils.getValueTooltip(utils, feature.maxEmptyCornersAllowed()).build(Lang.Value.MAX_EMPTY_CORNERS_ALLOWED));
        }, Lang.Feature.FOSSIL);
    }

    @NotNull
    public static TooltipBuilder getFreezeTopLayerTooltip(IServerUtils ignoredUtils, SnowAndFreezeFeature ignoredFeature) {
        return array(TooltipBuilder::showEmpty, Lang.Feature.FREEZE_TOP_LAYER);
    }

    @NotNull
    public static TooltipBuilder getGeodeTooltip(IServerUtils utils, GeodeFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.blockSettings()).build(Lang.Branch.GEODE_BLOCK_SETTINGS));
            b.add(utils.getValueTooltip(utils, feature.layerSettings()).build(Lang.Branch.GEODE_LAYER_SETTINGS));
            b.add(utils.getValueTooltip(utils, feature.crackSettings()).build(Lang.Branch.GEODE_CRACK_SETTINGS));
            b.add(utils.getValueTooltip(utils, feature.usePotentialPlacementsChance()).build(Lang.Value.POTENTIAL_PLACEMENT_CHANCE));
            b.add(utils.getValueTooltip(utils, feature.useAlternateLayer0Chance()).build(Lang.Value.ALTERNATE_LAYER_CHANCE));
            b.add(utils.getValueTooltip(utils, feature.placementsRequireLayer0Alternate()).build(Lang.Value.REQUIRE_ALTERNATE_LAYER));
            b.add(utils.getValueTooltip(utils, feature.outerWallDistance()).build(Lang.Branch.OUTER_WALL_DISTANCE));
            b.add(utils.getValueTooltip(utils, feature.distributionPoints()).build(Lang.Branch.DISTRIBUTION_POINTS));
            b.add(utils.getValueTooltip(utils, feature.pointOffset()).build(Lang.Branch.POINT_OFFSET));
            b.add(utils.getValueTooltip(utils, feature.minGenOffset()).build(Lang.Value.MIN_GEN_OFFSET));
            b.add(utils.getValueTooltip(utils, feature.maxGenOffset()).build(Lang.Value.MAX_GEN_OFFSET));
            b.add(utils.getValueTooltip(utils, feature.noiseMultiplier()).build(Lang.Value.NOISE_MULTIPLIER));
            b.add(utils.getValueTooltip(utils, feature.invalidBlocksThreshold()).build(Lang.Value.INVALID_BLOCKS_THRESHOLD));
        }, Lang.Feature.GEODE);
    }

    @NotNull
    public static TooltipBuilder getHugeBrownMushroomTooltip(IServerUtils utils, HugeBrownMushroomFeature feature) {
        return getHugeMushroomTooltip(utils, feature, Lang.Feature.HUGE_BROWN_MUSHROOM);
    }

    @NotNull
    public static TooltipBuilder getHugeRedMushroomTooltip(IServerUtils utils, HugeRedMushroomFeature feature) {
        return getHugeMushroomTooltip(utils, feature, Lang.Feature.HUGE_RED_MUSHROOM);
    }

    @NotNull
    public static TooltipBuilder getHugeFungusTooltip(IServerUtils utils, HugeFungusFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.validBaseState()).build(Lang.Branch.VALID_BASE_STATE));
            b.add(utils.getValueTooltip(utils, feature.stemState()).build(Lang.Branch.STEM_STATE));
            b.add(utils.getValueTooltip(utils, feature.hatState()).build(Lang.Branch.HAT_STATE));
            b.add(utils.getValueTooltip(utils, feature.decorState()).build(Lang.Branch.DECOR_STATE));
            b.add(utils.getValueTooltip(utils, feature.replaceableBlocks()).build(Lang.Branch.REPLACEABLE_BLOCKS));
            b.add(utils.getValueTooltip(utils, feature.planted()).build(Lang.Value.PLANTED));
        }, Lang.Feature.HUGE_FUNGUS);
    }

    @NotNull
    public static TooltipBuilder getIcebergTooltip(IServerUtils utils, IcebergFeature feature) {
        return array((b) -> b.add(utils.getValueTooltip(utils, feature.state()).build(Lang.Branch.STATE)), Lang.Feature.ICEBERG);
    }

    @NotNull
    public static TooltipBuilder getLakeTooltip(IServerUtils utils, LakeFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.fluid()).build(Lang.Branch.FLUID));
            b.add(utils.getValueTooltip(utils, feature.barrier()).build(Lang.Branch.BARRIER));
            b.add(utils.getValueTooltip(utils, feature.canPlaceFeature()).build(Lang.Branch.CAN_PLACE_FEATURE));
            b.add(utils.getValueTooltip(utils, feature.canReplaceWithAirOrFluid()).build(Lang.Branch.CAN_REPLACE_WITH_AIR_OR_FLUID));
            b.add(utils.getValueTooltip(utils, feature.canReplaceWithBarrier()).build(Lang.Branch.CAN_REPLACE_WITH_BARRIER));
        }, Lang.Feature.LAKE);
    }

    @NotNull
    public static TooltipBuilder getLargeDripstoneTooltip(IServerUtils utils, LargeDripstoneFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.replaceableBlocks()).build(Lang.Branch.REPLACEABLE_BLOCKS));
            b.add(utils.getValueTooltip(utils, feature.floorToCeilingSearchRange()).build(Lang.Value.SEARCH_RANGE));
            b.add(utils.getValueTooltip(utils, feature.columnRadius()).build(Lang.Branch.COLUMN_RADIUS));
            b.add(utils.getValueTooltip(utils, feature.heightScale()).build(Lang.Branch.HEIGHT_SCALE));
            b.add(utils.getValueTooltip(utils, feature.maxColumnRadiusToCaveHeightRatio()).build(Lang.Value.RADIUS_TO_HEIGHT_RATIO));
            b.add(utils.getValueTooltip(utils, feature.stalactiteBluntness()).build(Lang.Branch.STALACTITE_BLUNTNESS));
            b.add(utils.getValueTooltip(utils, feature.stalagmiteBluntness()).build(Lang.Branch.STALAGMITE_BLUNTNESS));
            b.add(utils.getValueTooltip(utils, feature.windSpeed()).build(Lang.Branch.WIND_SPEED));
            b.add(utils.getValueTooltip(utils, feature.minRadiusForWind()).build(Lang.Value.MIN_RADIUS_FOR_WIND));
            b.add(utils.getValueTooltip(utils, feature.minBluntnessForWind()).build(Lang.Value.MIN_BLUNTNESS_FOR_WIND));
        }, Lang.Feature.LARGE_DRIPSTONE);
    }

    @NotNull
    public static TooltipBuilder getMonsterRoomTooltip(IServerUtils ignoredUtils, MonsterRoomFeature ignoredFeature) {
        return array(TooltipBuilder::showEmpty, Lang.Feature.MONSTER_ROOM);
    }

    @NotNull
    public static TooltipBuilder getMultifaceGrowthTooltip(IServerUtils utils, MultifaceGrowthFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.placeBlock()).build(Lang.Value.PLACE_BLOCK));
            b.add(utils.getValueTooltip(utils, feature.searchRange()).build(Lang.Value.SEARCH_RANGE));
            b.add(utils.getValueTooltip(utils, feature.canPlaceOnFloor()).build(Lang.Value.CAN_PLACE_ON_FLOOR));
            b.add(utils.getValueTooltip(utils, feature.canPlaceOnCeiling()).build(Lang.Value.CAN_PLACE_ON_CEILING));
            b.add(utils.getValueTooltip(utils, feature.canPlaceOnWall()).build(Lang.Value.CAN_PLACE_ON_WALL));
            b.add(utils.getValueTooltip(utils, feature.chanceOfSpreading()).build(Lang.Value.CHANCE_OF_SPREADING));
            b.add(utils.getValueTooltip(utils, feature.canBePlacedOn()).build(Lang.Branch.CAN_BE_PLACED_ON));
        }, Lang.Feature.MULTIFACE_GROWTH);
    }

    @NotNull
    public static TooltipBuilder getNetherrackReplaceBlobsTooltip(IServerUtils utils, ReplaceBlobsFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.targetState()).build(Lang.Branch.TARGET_STATE));
            b.add(utils.getValueTooltip(utils, feature.replaceState()).build(Lang.Branch.REPLACE_STATE));
            b.add(utils.getValueTooltip(utils, feature.radius()).build(Lang.Branch.RADIUS));
        }, Lang.Feature.NETHERRACK_REPLACE_BLOBS);
    }

    @NotNull
    public static TooltipBuilder getNoOpTooltip(IServerUtils ignoredUtils, NoOpFeature ignoredFeature) {
        return array(TooltipBuilder::showEmpty, Lang.Feature.NO_OP);
    }

    @NotNull
    public static TooltipBuilder getOreTooltip(IServerUtils utils, OreFeature feature) {
        return getAbstractOreTooltip(utils, feature, Lang.Feature.ORE);
    }

    @NotNull
    public static TooltipBuilder getScatteredOreTooltip(IServerUtils utils, ScatteredOreFeature feature) {
        return getAbstractOreTooltip(utils, feature, Lang.Feature.SCATTERED_ORE);
    }

    @NotNull
    public static TooltipBuilder getOverlayTooltip(IServerUtils utils, OverlayFeature feature) {
        return array((b) -> b.add(utils.getValueTooltip(utils, feature.features()).build(Lang.Branch.FEATURES)), Lang.Feature.OVERLAY);
    }

    @NotNull
    public static TooltipBuilder getProjectedRandomPatchySquareTooltip(IServerUtils utils, ProjectedRandomPatchySquare feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.block()).build(Lang.Branch.BLOCK));
            b.add(utils.getValueTooltip(utils, feature.projectThrough()).build(Lang.Branch.PROJECT_THROUGH));
            b.add(utils.getValueTooltip(utils, feature.size()).build(Lang.Branch.SIZE));
            b.add(utils.getValueTooltip(utils, feature.maxProjectionHeight()).build(Lang.Value.MAX_PROJECTION_HEIGHT));
        }, Lang.Feature.PROJECTED_RANDOM_PATCHY_SQUARE);
    }

    @NotNull
    public static TooltipBuilder getRandomBooleanSelectorTooltip(IServerUtils utils, RandomBooleanSelectorFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.featureTrue()).build(Lang.Branch.FEATURE_TRUE));
            b.add(utils.getValueTooltip(utils, feature.featureFalse()).build(Lang.Branch.FEATURE_FALSE));
        }, Lang.Feature.RANDOM_BOOLEAN_SELECTOR);
    }

    @NotNull
    public static TooltipBuilder getRandomNeighborSpreadTooltip(IServerUtils utils, RandomNeighborSpreadFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.block()).build(Lang.Branch.BLOCK));
            b.add(utils.getValueTooltip(utils, feature.acceptedNeighbors()).build(Lang.Branch.ACCEPTED_NEIGHBORS));
            b.add(utils.getValueTooltip(utils, feature.canReplace()).build(Lang.Branch.CAN_REPLACE));
            b.add(utils.getValueTooltip(utils, feature.attempts()).build(Lang.Branch.ATTEMPTS));
            b.add(utils.getValueTooltip(utils, feature.xzOffset()).build(Lang.Branch.XZ_OFFSET));
            b.add(utils.getValueTooltip(utils, feature.yOffset()).build(Lang.Branch.Y_OFFSET));
        }, Lang.Feature.RANDOM_NEIGHBOR_SPREAD);
    }

    @NotNull
    public static TooltipBuilder getRandomSelectorTooltip(IServerUtils utils, RandomSelectorFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.features()).build(Lang.Branch.FEATURES));
            b.add(utils.getValueTooltip(utils, feature.defaultFeature()).build(Lang.Branch.DEFAULT_FEATURE));
        }, Lang.Feature.RANDOM_SELECTOR);
    }

    @NotNull
    public static TooltipBuilder getReplaceSingleBlockTooltip(IServerUtils utils, ReplaceBlockFeature feature) {
        return array((b) -> b.add(utils.getValueTooltip(utils, feature.replacements()).build(Lang.Branch.REPLACEMENTS)), Lang.Feature.REPLACE_SINGLE_BLOCK);
    }

    @NotNull
    public static TooltipBuilder getRootSystemTooltip(IServerUtils utils, RootSystemFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.treeFeature()).build(Lang.Branch.TREE_FEATURE));
            b.add(utils.getValueTooltip(utils, feature.requiredVerticalSpaceForTree()).build(Lang.Value.REQUIRED_VERTICAL_SPACE_FOR_TREE));
            b.add(utils.getValueTooltip(utils, feature.levelTestDistance()).build(Lang.Value.LEVEL_TEST_DISTANCE));
            b.add(utils.getValueTooltip(utils, feature.maxLevelDeviation()).build(Lang.Value.MAX_LEVEL_DEVIATION));
            b.add(utils.getValueTooltip(utils, feature.rootRadius()).build(Lang.Value.ROOT_RADIUS));
            b.add(utils.getValueTooltip(utils, feature.rootReplaceable()).build(Lang.Branch.ROOT_REPLACEABLE));
            b.add(utils.getValueTooltip(utils, feature.rootStateProvider()).build(Lang.Branch.ROOT_STATE_PROVIDER));
            b.add(utils.getValueTooltip(utils, feature.rootPlacementAttempts()).build(Lang.Value.ROOT_PLACEMENT_ATTEMPTS));
            b.add(utils.getValueTooltip(utils, feature.rootColumnMaxHeight()).build(Lang.Value.ROOT_COLUMN_MAX_HEIGHT));
            b.add(utils.getValueTooltip(utils, feature.hangingRootRadius()).build(Lang.Value.HANGING_ROOT_RADIUS));
            b.add(utils.getValueTooltip(utils, feature.hangingRootsVerticalSpan()).build(Lang.Value.HANGING_ROOT_VERTICAL_SPAN));
            b.add(utils.getValueTooltip(utils, feature.hangingRootStateProvider()).build(Lang.Branch.HANGING_ROOT_STATE_PROVIDER));
            b.add(utils.getValueTooltip(utils, feature.hangingRootPlacementAttempts()).build(Lang.Value.HANGING_ROOT_PLACEMENT_ATTEMPTS));
            b.add(utils.getValueTooltip(utils, feature.allowedVerticalWaterForTree()).build(Lang.Value.ALLOWED_VERTICAL_WATER_FOR_TREE));
            b.add(utils.getValueTooltip(utils, feature.allowedTreePosition()).build(Lang.Branch.ALLOWED_TREE_POSITION));
        }, Lang.Feature.ROOT_SYSTEM);
    }

    @NotNull
    public static TooltipBuilder getSculkPatchTooltip(IServerUtils utils, SculkPatchFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.chargeCount()).build(Lang.Value.CHARGE_COUNT));
            b.add(utils.getValueTooltip(utils, feature.amountPerCharge()).build(Lang.Value.AMOUNT_PER_CHARGE));
            b.add(utils.getValueTooltip(utils, feature.spreadAttempts()).build(Lang.Value.SPREAD_ATTEMPTS));
            b.add(utils.getValueTooltip(utils, feature.growthRounds()).build(Lang.Value.GROWTH_ROUNDS));
            b.add(utils.getValueTooltip(utils, feature.spreadRounds()).build(Lang.Value.SPREAD_ROUNDS));
        }, Lang.Feature.SCULK_PATCH);
    }

    @NotNull
    public static TooltipBuilder getSequenceTooltip(IServerUtils utils, SequenceFeature feature) {
        return array((b) -> b.add(utils.getValueTooltip(utils, feature.features()).build(Lang.Branch.FEATURES)), Lang.Feature.SEQUENCE);
    }

    @NotNull
    public static TooltipBuilder getSimpleBlockTooltip(IServerUtils utils, SimpleBlockFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.toPlace()).build(Lang.Branch.TO_PLACE));
            b.add(utils.getValueTooltip(utils, feature.scheduleTick()).build(Lang.Value.SCHEDULE_TICK));
        }, Lang.Feature.SIMPLE_BLOCK);
    }

    @NotNull
    public static TooltipBuilder getSimpleRandomSelectorTooltip(IServerUtils utils, SimpleRandomSelectorFeature feature) {
        return array((b) -> b.add(utils.getValueTooltip(utils, feature.features()).build(Lang.Branch.FEATURES)), Lang.Feature.SIMPLE_RANDOM_SELECTOR);
    }

    @NotNull
    public static TooltipBuilder getSingleBlockPillarTooltip(IServerUtils utils, SingleBlockPillarFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.block()).build(Lang.Branch.BLOCK));
            b.add(utils.getValueTooltip(utils, feature.canReplace()).build(Lang.Branch.CAN_REPLACE));
            b.add(utils.getValueTooltip(utils, feature.direction()).build(Lang.Value.DIRECTION));
            b.add(utils.getValueTooltip(utils, feature.chanceToContinue()).build(Lang.Value.CHANCE_TO_CONTINUE));
            b.add(utils.getValueTooltip(utils, feature.capFeature()).build(Lang.Branch.CAP_FEATURE));
        }, Lang.Feature.SINGLE_BLOCK_PILLAR);
    }

    @NotNull
    public static TooltipBuilder getSpeleothemTooltip(IServerUtils utils, SpeleothemFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.baseBlock()).build(Lang.Branch.BASE_BLOCK));
            b.add(utils.getValueTooltip(utils, feature.pointedBlock()).build(Lang.Branch.POINTED_BLOCK));
            b.add(utils.getValueTooltip(utils, feature.replaceableBlocks()).build(Lang.Branch.REPLACEABLE_BLOCKS));
            b.add(utils.getValueTooltip(utils, feature.chanceOfTallerGeneration()).build(Lang.Value.CHANCE_OF_TALLER_GENERATION));
            b.add(utils.getValueTooltip(utils, feature.chanceOfDirectionalSpread()).build(Lang.Value.CHANCE_OF_DIRECTIONAL_SPREAD));
            b.add(utils.getValueTooltip(utils, feature.chanceOfSpreadRadius2()).build(Lang.Value.CHANCE_OF_SPREAD_RADIUS_2));
            b.add(utils.getValueTooltip(utils, feature.chanceOfSpreadRadius3()).build(Lang.Value.CHANCE_OF_SPREAD_RADIUS_3));
        }, Lang.Feature.SPELEOTHEM);
    }

    @NotNull
    public static TooltipBuilder getSpeleothemClusterTooltip(IServerUtils utils, SpeleothemClusterFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.baseBlock()).build(Lang.Branch.BASE_BLOCK));
            b.add(utils.getValueTooltip(utils, feature.pointedBlock()).build(Lang.Branch.POINTED_BLOCK));
            b.add(utils.getValueTooltip(utils, feature.replaceableBlocks()).build(Lang.Branch.REPLACEABLE_BLOCKS));
            b.add(utils.getValueTooltip(utils, feature.floorToCeilingSearchRange()).build(Lang.Value.FLOOR_TO_CEILING_SEARCH_RANGE));
            b.add(utils.getValueTooltip(utils, feature.height()).build(Lang.Branch.HEIGHT));
            b.add(utils.getValueTooltip(utils, feature.radius()).build(Lang.Branch.RADIUS));
            b.add(utils.getValueTooltip(utils, feature.maxStalagmiteStalactiteHeightDiff()).build(Lang.Value.MAX_STALAGMITE_STALACTITE_HEIGHT_DIFF));
            b.add(utils.getValueTooltip(utils, feature.heightDeviation()).build(Lang.Value.HEIGHT_DEVIATION));
            b.add(utils.getValueTooltip(utils, feature.speleothemBlockLayerThickness()).build(Lang.Branch.SPELEOTHEM_BLOCK_LAYER_THICKNESS));
            b.add(utils.getValueTooltip(utils, feature.density()).build(Lang.Branch.DENSITY));
            b.add(utils.getValueTooltip(utils, feature.wetness()).build(Lang.Branch.WETNESS));
            b.add(utils.getValueTooltip(utils, feature.chanceOfSpeleothemAtMaxDistanceFromCenter()).build(Lang.Value.CHANCE_OF_SPELEOTHEM_AT_MAX_DISTANCE_FROM_CENTER));
            b.add(utils.getValueTooltip(utils, feature.maxDistanceFromEdgeAffectingChanceOfSpeleothem()).build(Lang.Value.MAX_DISTANCE_FROM_EDGE_AFFECTING_CHANCE_OF_SPELEOTHEM));
            b.add(utils.getValueTooltip(utils, feature.maxDistanceFromCenterAffectingHeightBias()).build(Lang.Value.MAX_DISTANCE_FROM_CENTER_AFFECTING_HEIGHT_BIAS));
        }, Lang.Feature.SPELEOTHEM_CLUSTER);
    }

    @NotNull
    public static TooltipBuilder getSpikeTooltip(IServerUtils utils, SpikeFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.state()).build(Lang.Branch.STATE));
            b.add(utils.getValueTooltip(utils, feature.canPlaceOn()).build(Lang.Branch.CAN_PLACE_ON));
            b.add(utils.getValueTooltip(utils, feature.canReplace()).build(Lang.Branch.CAN_REPLACE));
        }, Lang.Feature.SPIKE);
    }

    @NotNull
    public static TooltipBuilder getSpringTooltip(IServerUtils utils, SpringFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.state()).build(Lang.Branch.STATE));
            b.add(utils.getValueTooltip(utils, feature.requiresBlockBelow()).build(Lang.Value.REQUIRES_BLOCK_BELOW));
            b.add(utils.getValueTooltip(utils, feature.rockCount()).build(Lang.Value.ROCK_COUNT));
            b.add(utils.getValueTooltip(utils, feature.holeCount()).build(Lang.Value.HOLE_COUNT));
            b.add(utils.getValueTooltip(utils, feature.validBlocks()).build(Lang.Branch.VALID_BLOCKS));
        }, Lang.Feature.SPRING_FEATURE);
    }

    @NotNull
    public static TooltipBuilder getSteppedColumnClusterTooltip(IServerUtils utils, SteppedColumnClusterFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.block()).build(Lang.Branch.BLOCK));
            b.add(utils.getValueTooltip(utils, feature.continueThrough()).build(Lang.Branch.CONTINUE_THROUGH));
            b.add(utils.getValueTooltip(utils, feature.canReplace()).build(Lang.Branch.CAN_REPLACE));
            b.add(utils.getValueTooltip(utils, feature.cannotPlaceOn()).build(Lang.Branch.CANNOT_PLACE_ON));
            b.add(utils.getValueTooltip(utils, feature.clusterReach()).build(Lang.Branch.CLUSTER_REACH));
            b.add(utils.getValueTooltip(utils, feature.columnCount()).build(Lang.Branch.COLUMN_COUNT));
            b.add(utils.getValueTooltip(utils, feature.columnReach()).build(Lang.Branch.COLUMN_REACH));
            b.add(utils.getValueTooltip(utils, feature.height()).build(Lang.Branch.HEIGHT));
        }, Lang.Feature.STEPPED_COLUMN_CLUSTER);
    }

    @NotNull
    public static TooltipBuilder getTemplateTooltip(IServerUtils utils, TemplateFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.templates()).build(Lang.Branch.TEMPLATES));
            b.add(utils.getValueTooltip(utils, feature.processors()).build(Lang.Branch.PROCESSORS));
        }, Lang.Feature.TEMPLATE);
    }

    @NotNull
    public static TooltipBuilder getTreeTooltip(IServerUtils utils, TreeFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.trunkProvider()).build(Lang.Branch.TRUNK_PROVIDER));
            b.add(utils.getValueTooltip(utils, feature.trunkPlacer()).build(Lang.Branch.TRUNK_PLACER));
            b.add(utils.getValueTooltip(utils, feature.foliageProvider()).build(Lang.Branch.FOLIAGE_PROVIDER));
            b.add(utils.getValueTooltip(utils, feature.foliagePlacer()).build(Lang.Branch.FOLIAGE_PLACER));
            b.add(utils.getValueTooltip(utils, feature.rootPlacer()).build(Lang.Branch.ROOT_PLACER));
            b.add(utils.getValueTooltip(utils, feature.minimumSize()).build(Lang.Branch.MINIMUM_SIZE));
            b.add(utils.getValueTooltip(utils, feature.decorators()).build(Lang.Branch.DECORATORS));
            b.add(utils.getValueTooltip(utils, feature.ignoreVines()).build(Lang.Value.IGNORE_VINES));
            b.add(utils.getValueTooltip(utils, feature.belowTrunkProvider()).build(Lang.Branch.BELOW_TRUNK_PROVIDER));
        }, Lang.Feature.TREE);
    }

    @NotNull
    public static TooltipBuilder getUnderwaterMagmaTooltip(IServerUtils utils, UnderwaterMagmaFeature feature) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.floorSearchRange()).build(Lang.Value.FLOOR_RANGE_SEARCH));
            b.add(utils.getValueTooltip(utils, feature.placementRadiusAroundFloor()).build(Lang.Value.PLACEMENT_RADIUS_AROUND_FLOOR));
            b.add(utils.getValueTooltip(utils, feature.placementProbabilityPerValidPosition()).build(Lang.Value.PROBABILITY_PER_POSITION));
        }, Lang.Feature.UNDERWATER_MAGMA);
    }

    @NotNull
    public static TooltipBuilder getVegetationPatchTooltip(IServerUtils utils, VegetationPatchFeature feature) {
        return getVegetationPatchTooltip(utils, feature, Lang.Feature.VEGETATION_PATCH);
    }

    @NotNull
    public static TooltipBuilder getWaterloggedVegetationPatchTooltip(IServerUtils utils, WaterloggedVegetationPatchFeature feature) {
        return getVegetationPatchTooltip(utils, feature, Lang.Feature.WATERLOGGED_VEGETATION_PATCH);
    }

    @NotNull
    public static TooltipBuilder getVinesTooltip(IServerUtils ignoredUtils, VinesFeature ignoredFeature) {
        return array(TooltipBuilder::showEmpty, Lang.Feature.VINES);
    }

    @NotNull
    public static TooltipBuilder getVoidStartPlatformTooltip(IServerUtils ignoredUtils, VoidStartPlatformFeature ignoredFeature) {
        return array(TooltipBuilder::showEmpty, Lang.Feature.VOID_START_PLATFORM);
    }

    @NotNull
    public static TooltipBuilder getWeightedRandomSelectorTooltip(IServerUtils utils, WeightedRandomSelectorFeature feature) {
        return array((b) -> b.add(utils.getValueTooltip(utils, feature.features()).build(Lang.Branch.FEATURES)), Lang.Feature.WEIGHTED_RANDOM_SELECTOR);
    }

    @NotNull
    private static TooltipBuilder getAbstractOreTooltip(IServerUtils utils, AbstractOreFeature feature, Lang.Feature key) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.targetStates()).build(Lang.Branch.TARGET_STATES));
            b.add(utils.getValueTooltip(utils, feature.size()).build(Lang.Value.SIZE));
            b.add(utils.getValueTooltip(utils, feature.discardChanceOnAirExposure()).build(Lang.Value.DISCARD_CHANCE_ON_AIR_EXPOSURE));
        }, key);
    }

    @NotNull
    private static TooltipBuilder getHugeMushroomTooltip(IServerUtils utils, AbstractHugeMushroomFeature feature, Lang.Feature key) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.capProvider()).build(Lang.Branch.CAP_PROVIDER));
            b.add(utils.getValueTooltip(utils, feature.stemProvider()).build(Lang.Branch.STEM_PROVIDER));
            b.add(utils.getValueTooltip(utils, feature.foliageRadius()).build(Lang.Value.FOLIAGE_RADIUS));
            b.add(utils.getValueTooltip(utils, feature.canPlaceOn()).build(Lang.Branch.CAN_PLACE_ON));
        }, key);
    }

    @NotNull
    private static TooltipBuilder getVegetationPatchTooltip(IServerUtils utils, VegetationPatchFeature feature, Lang.Feature key) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, feature.replaceable).build(Lang.Branch.REPLACEABLE));
            b.add(utils.getValueTooltip(utils, feature.groundState).build(Lang.Branch.GROUND_STATE));
            b.add(utils.getValueTooltip(utils, feature.vegetationFeature).build(Lang.Branch.VEGETATION_FEATURE));
            b.add(utils.getValueTooltip(utils, feature.surface).build(Lang.Value.SURFACE));
            b.add(utils.getValueTooltip(utils, feature.depth).build(Lang.Branch.DEPTH));
            b.add(utils.getValueTooltip(utils, feature.extraBottomBlockChance).build(Lang.Value.EXTRA_BOTTOM_BLOCK_CHANCE));
            b.add(utils.getValueTooltip(utils, feature.verticalRange).build(Lang.Value.VERTICAL_RANGE));
            b.add(utils.getValueTooltip(utils, feature.vegetationChance).build(Lang.Value.VEGETATION_CHANCE));
            b.add(utils.getValueTooltip(utils, feature.xzRadius).build(Lang.Branch.XZ_RADIUS));
            b.add(utils.getValueTooltip(utils, feature.extraEdgeColumnChance).build(Lang.Value.EXTRA_EDGE_COLUMN_CHANCE));
        }, key);
    }
}
