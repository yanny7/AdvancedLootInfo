package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.advancements.predicates.entity.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getMapTooltip;

public class EntitySubPredicateTooltipUtils {
    @NotNull
    public static TooltipBuilder getLightningBoltPredicateTooltip(IServerUtils utils, LightningBoltPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.blocksSetOnFire()).build(Lang.Value.BLOCKS_ON_FIRE));
            b.add(utils.getValueTooltip(utils, predicate.entityStruck()).build(Lang.Branch.STUCK_ENTITY));
        }, Lang.EntitySubPredicates.LIGHTNING_BOLT);
    }

    @NotNull
    public static TooltipBuilder getFishingHookPredicateTooltip(IServerUtils utils, FishingHookPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.inOpenWater()).build(Lang.Value.IN_OPEN_WATER));
            b.showEmpty();
        }, Lang.EntitySubPredicates.FISHING_HOOK);
    }

    @NotNull
    public static TooltipBuilder getPlayerPredicateTooltip(IServerUtils utils, PlayerPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.level()).build(Lang.Value.LEVEL));
            b.add(utils.getValueTooltip(utils, predicate.food()).build(Lang.Branch.FOOD));
            b.add(utils.getValueTooltip(utils, predicate.gameType()).build(Lang.Branch.GAME_TYPES));
            b.add(utils.getValueTooltip(utils, predicate.stats()).build(Lang.Branch.STATS));
            b.add(GenericTooltipUtils.getMapTooltip(utils, predicate.recipes(), GenericTooltipUtils::getRecipeEntryTooltip).build(Lang.Branch.RECIPES));
            b.add(GenericTooltipUtils.getMapTooltip(utils, predicate.advancements(), GenericTooltipUtils::getAdvancementEntryTooltip).build(Lang.Branch.ADVANCEMENTS));
            b.add(utils.getValueTooltip(utils, predicate.lookingAt()).build(Lang.Branch.LOOKING_AT));
            b.add(utils.getValueTooltip(utils, predicate.input()).build(Lang.Branch.INPUT));
        }, Lang.EntitySubPredicates.PLAYER);
    }

    @NotNull
    public static TooltipBuilder getCubeMobTooltip(IServerUtils utils, CubeMobPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.size()).build(Lang.Value.SIZE)), Lang.EntitySubPredicates.CUBE_MOB);
    }

    @NotNull
    public static TooltipBuilder getRaiderPredicateTooltip(IServerUtils utils, RaiderPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.hasRaid()).build(Lang.Value.HAS_RAID));
            b.add(utils.getValueTooltip(utils, predicate.isCaptain()).build(Lang.Value.IS_CAPTAIN));
        }, Lang.EntitySubPredicates.RAIDER);
    }

    @NotNull
    public static TooltipBuilder getSheepPredicateTooltip(IServerUtils utils, SheepPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.sheared()).build(Lang.Value.SHEARED)), Lang.EntitySubPredicates.SHEEP);
    }

    @NotNull
    public static TooltipBuilder getEntityTypeTooltip(IServerUtils utils, EntityTypePredicate predicate) {
        return utils.getValueTooltip(utils, predicate.types()).key(Lang.EntitySubPredicates.ENTITY_TYPE);
    }

    @NotNull
    public static TooltipBuilder getEntityLocationTooltip(IServerUtils utils, EntityLocationPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.predicate()).key(Lang.EntitySubPredicates.LOCATION);
    }

    @NotNull
    public static TooltipBuilder getSteppingOnTooltip(IServerUtils utils, SteppingOnPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.predicate()).key(Lang.EntitySubPredicates.STEPPING_ON);
    }

    @NotNull
    public static TooltipBuilder getMovementAffectedByTooltip(IServerUtils utils, MovementAffectedByPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.predicate()).key(Lang.EntitySubPredicates.MOVEMENT_AFFECTED_BY);
    }

    @NotNull
    public static TooltipBuilder getDistanceToPlayerTooltip(IServerUtils utils, DistanceToPlayerPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.distance()).key(Lang.EntitySubPredicates.DISTANCE_TO_PLAYER);
    }

    @NotNull
    public static TooltipBuilder getMovementTooltip(IServerUtils utils, MovementPredicate predicate) {
        return utils.getValueTooltip(utils, predicate).key(Lang.EntitySubPredicates.MOVEMENT);
    }

    @NotNull
    public static TooltipBuilder getEntityEffectsTooltip(IServerUtils utils, EntityEffectsPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.effects()).key(Lang.EntitySubPredicates.EFFECTS);
    }

    @NotNull
    public static TooltipBuilder getNbtTooltip(IServerUtils utils, EntityNbtPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.nbt()).key(Lang.EntitySubPredicates.NBT);
    }

    @NotNull
    public static TooltipBuilder getFlagsTooltip(IServerUtils utils, EntityFlagsPredicate predicate) {
        return utils.getValueTooltip(utils, predicate).key(Lang.EntitySubPredicates.FLAGS);
    }

    @NotNull
    public static TooltipBuilder getEquipmentTooltip(IServerUtils utils, EntityEquipmentPredicate predicate) {
        return utils.getValueTooltip(utils, predicate).key(Lang.EntitySubPredicates.EQUIPMENT);
    }

    @NotNull
    public static TooltipBuilder getPeriodicTickTooltip(IServerUtils utils, PeriodicEntityTickPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.periodicTick()).key(Lang.EntitySubPredicates.PERIODIC_TICK);
    }

    @NotNull
    public static TooltipBuilder getVehicleTooltip(IServerUtils utils, VehiclePredicate predicate) {
        return utils.getValueTooltip(utils, predicate.vehicle()).key(Lang.EntitySubPredicates.VEHICLE);
    }

    @NotNull
    public static TooltipBuilder getPassengerTooltip(IServerUtils utils, PassengerPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.passenger()).key(Lang.EntitySubPredicates.PASSENGER);
    }

    @NotNull
    public static TooltipBuilder getTargetedEntityTooltip(IServerUtils utils, TargetedEntityPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.targetedEntity()).key(Lang.EntitySubPredicates.TARGETED_ENTITY);
    }

    @NotNull
    public static TooltipBuilder getTeamTooltip(IServerUtils utils, TeamPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.team()).key(Lang.EntitySubPredicates.TEAM);
    }

    @NotNull
    public static TooltipBuilder getSlotsTooltip(IServerUtils utils, EntitySlotsPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.slots()).key(Lang.EntitySubPredicates.SLOTS);
    }

    @NotNull
    public static TooltipBuilder getComponentsTooltip(IServerUtils utils, EntityExactDataComponentsPredicate predicate) {
        return utils.getValueTooltip(utils, predicate.predicate()).key(Lang.EntitySubPredicates.COMPONENTS);
    }

    @NotNull
    public static TooltipBuilder getPredicatesTooltip(IServerUtils utils, EntityPartialComponentsPredicate predicate) {
        return getMapTooltip(utils, predicate.predicates(), GenericTooltipUtils::getDataComponentPredicateEntryTooltip).key(Lang.EntitySubPredicates.PREDICATES);
    }

    @NotNull
    public static TooltipBuilder getEntityTagsTooltip(IServerUtils utils, EntityTagPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            predicate.anyOf().ifPresent((tag) -> b.add(utils.getValueTooltip(utils, tag)).build(Lang.Branch.ANY_OF));
            predicate.allOf().ifPresent((tag) -> b.add(utils.getValueTooltip(utils, tag)).build(Lang.Branch.ALL_OF));
            predicate.noneOf().ifPresent((tag) -> b.add(utils.getValueTooltip(utils, tag)).build(Lang.Branch.NONE_OF));
        }).key(Lang.EntitySubPredicates.ENTITY_TAGS);
    }
}
