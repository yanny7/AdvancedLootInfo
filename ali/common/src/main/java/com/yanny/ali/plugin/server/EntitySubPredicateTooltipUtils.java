package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.advancements.critereon.*;
import org.jetbrains.annotations.NotNull;

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
            b.add(utils.getValueTooltip(utils, predicate.gameType()).build(Lang.Branch.GAME_TYPES));
            b.add(utils.getValueTooltip(utils, predicate.stats()).build(Lang.Branch.STATS));
            b.add(GenericTooltipUtils.getMapTooltip(utils, predicate.recipes(), GenericTooltipUtils::getRecipeEntryTooltip).build(Lang.Branch.RECIPES));
            b.add(GenericTooltipUtils.getMapTooltip(utils, predicate.advancements(), GenericTooltipUtils::getAdvancementEntryTooltip).build(Lang.Branch.ADVANCEMENTS));
            b.add(utils.getValueTooltip(utils, predicate.lookingAt()).build(Lang.Branch.LOOKING_AT));
            b.add(utils.getValueTooltip(utils, predicate.input()).build(Lang.Branch.INPUT));
        }, Lang.EntitySubPredicates.PLAYER);
    }

    @NotNull
    public static TooltipBuilder getSlimePredicateTooltip(IServerUtils utils, SlimePredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.size()).build(Lang.Value.SIZE)), Lang.EntitySubPredicates.SLIME);
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
}
