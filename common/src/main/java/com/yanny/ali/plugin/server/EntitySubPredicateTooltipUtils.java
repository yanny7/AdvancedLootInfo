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
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, predicate.blocksSetOnFire()).build("ali.property.value.blocks_on_fire"))
                .add(utils.getValueTooltip(utils, predicate.entityStruck()).build("ali.property.branch.stuck_entity"))
                .build("ali.type.entity_sub_predicate.lightning_bolt");
    }

    @NotNull
    public static ITooltipNode getFishingHookPredicateTooltip(IServerUtils utils, FishingHookPredicate predicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, predicate.inOpenWater()).build("ali.property.value.in_open_water"))
                .build("ali.type.entity_sub_predicate.fishing_hook");
    }

    @NotNull
    public static ITooltipNode getPlayerPredicateTooltip(IServerUtils utils, PlayerPredicate predicate) {
        return BranchTooltipNode.branch("ali.type.entity_sub_predicate.player")
                .add(utils.getValueTooltip(utils, predicate.level()).build("ali.property.value.level"))
                .add(utils.getValueTooltip(utils, predicate.gameType()).build("ali.property.branch.game_types"))
                .add(GenericTooltipUtils.getCollectionTooltip(utils, predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip).build("ali.property.branch.stats"))
                .add(GenericTooltipUtils.getMapTooltip(utils, predicate.recipes(), GenericTooltipUtils::getRecipeEntryTooltip).build("ali.property.branch.recipes"))
                .add(GenericTooltipUtils.getMapTooltip(utils, predicate.advancements(), GenericTooltipUtils::getAdvancementEntryTooltip).build("ali.property.branch.advancements"))
                .add(utils.getValueTooltip(utils, predicate.lookingAt()).build("ali.property.branch.looking_at"))
                .build("ali.type.entity_sub_predicate.player");
    }

    @NotNull
    public static ITooltipNode getSlimePredicateTooltip(IServerUtils utils, SlimePredicate predicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, predicate.size()).build("ali.property.value.size"))
                .build("ali.type.entity_sub_predicate.slime");
    }

    @NotNull
    public static ITooltipNode getRaiderPredicateTooltip(IServerUtils utils, RaiderPredicate predicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, predicate.hasRaid()).build("ali.property.value.has_raid"))
                .add(utils.getValueTooltip(utils, predicate.isCaptain()).build("ali.property.value.is_captain"))
                .build("ali.type.entity_sub_predicate.raider");
    }

    @NotNull
    public static <V> ITooltipNode getVariantPredicateTooltip(IServerUtils utils, EntitySubPredicates.EntityVariantPredicateType<V>.Instance predicate) {
        IKeyTooltipNode tooltip = RegistriesTooltipUtils.getEntitySubPredicateTooltip(utils, predicate);

        if (predicate.variant instanceof Enum<?> variant) {
            tooltip.add(utils.getValueTooltip(utils, variant).build("ali.property.value.variant"));
        }

        return tooltip.build("ali.property.value.type");
    }

    @NotNull
    public static <V> ITooltipNode getHolderVariantPredicateTooltip(IServerUtils utils, EntitySubPredicates.EntityHolderVariantPredicateType<V>.Instance predicate) {
        return RegistriesTooltipUtils.getEntitySubPredicateTooltip(utils, predicate)
                .add(utils.getValueTooltip(utils, predicate.variants).build("ali.property.branch.variants"))
                .build("ali.property.value.type");
    }
}
