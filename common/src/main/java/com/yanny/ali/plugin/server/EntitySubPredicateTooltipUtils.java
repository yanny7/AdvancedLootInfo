package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import net.minecraft.advancements.critereon.*;
import org.jetbrains.annotations.NotNull;

public class EntitySubPredicateTooltipUtils {
    @NotNull
    public static ITooltipNode getLightningBoltPredicateTooltip(IServerUtils utils, LightningBoltPredicate predicate) {
        return BranchTooltipNode.branch("ali.type.entity_sub_predicate.lightning_bolt")
                .add(utils.getValueTooltip(utils, predicate.blocksSetOnFire()).key("ali.property.value.blocks_on_fire"))
                .add(utils.getValueTooltip(utils, predicate.entityStruck()).key("ali.property.branch.stuck_entity"));
    }

    @NotNull
    public static ITooltipNode getFishingHookPredicateTooltip(IServerUtils utils, FishingHookPredicate predicate) {
        return BranchTooltipNode.branch("ali.type.entity_sub_predicate.fishing_hook")
                .add(utils.getValueTooltip(utils, predicate.inOpenWater()).key("ali.property.value.in_open_water"));
    }

    @NotNull
    public static ITooltipNode getPlayerPredicateTooltip(IServerUtils utils, PlayerPredicate predicate) {
        return BranchTooltipNode.branch("ali.type.entity_sub_predicate.player")
                .add(utils.getValueTooltip(utils, predicate.level()).key("ali.property.value.level"))
                .add(utils.getValueTooltip(utils, predicate.gameType()).key("ali.property.branch.game_types"))
                .add(GenericTooltipUtils.getCollectionTooltip(utils, predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip).key("ali.property.branch.stats"))
                .add(GenericTooltipUtils.getMapTooltip(utils, predicate.recipes(), GenericTooltipUtils::getRecipeEntryTooltip).key("ali.property.branch.recipes"))
                .add(GenericTooltipUtils.getMapTooltip(utils, predicate.advancements(), GenericTooltipUtils::getAdvancementEntryTooltip).key("ali.property.branch.advancements"))
                .add(utils.getValueTooltip(utils, predicate.lookingAt()).key("ali.property.branch.looking_at"));
    }

    @NotNull
    public static ITooltipNode getSlimePredicateTooltip(IServerUtils utils, SlimePredicate predicate) {
        return BranchTooltipNode.branch("ali.type.entity_sub_predicate.slime")
                .add(utils.getValueTooltip(utils, predicate.size()).key("ali.property.value.size"));
    }

    @NotNull
    public static ITooltipNode getRaiderPredicateTooltip(IServerUtils utils, RaiderPredicate predicate) {
        return BranchTooltipNode.branch("ali.type.entity_sub_predicate.raider")
                .add(utils.getValueTooltip(utils, predicate.hasRaid()).key("ali.property.value.has_raid"))
                .add(utils.getValueTooltip(utils, predicate.isCaptain()).key("ali.property.value.is_captain"));
    }

    @NotNull
    public static <V> ITooltipNode getVariantPredicateTooltip(IServerUtils utils, EntitySubPredicates.EntityVariantPredicateType<V>.Instance predicate) {
        IKeyTooltipNode tooltip = RegistriesTooltipUtils.getEntitySubPredicateTooltip(utils, predicate).key("ali.property.value.type");

        if (predicate.variant instanceof Enum<?> variant) {
            tooltip.add(utils.getValueTooltip(utils, variant).key("ali.property.value.variant"));
        }

        return tooltip;
    }

    @NotNull
    public static <V> ITooltipNode getHolderVariantPredicateTooltip(IServerUtils utils, EntitySubPredicates.EntityHolderVariantPredicateType<V>.Instance predicate) {
        return RegistriesTooltipUtils.getEntitySubPredicateTooltip(utils, predicate).key("ali.property.value.type")
                .add(utils.getValueTooltip(utils, predicate.variants).key("ali.property.branch.variants"));
    }
}
