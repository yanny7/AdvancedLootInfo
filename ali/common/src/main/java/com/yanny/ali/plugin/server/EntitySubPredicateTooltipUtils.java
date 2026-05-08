package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.advancements.critereon.*;
import org.jetbrains.annotations.NotNull;

public class EntitySubPredicateTooltipUtils {
    @NotNull
    public static TooltipNode getLightningBoltPredicateTooltip(IServerUtils utils, LightningBoltPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, predicate.blocksSetOnFire()).build("ali.property.value.blocks_on_fire"))
                        .add(utils.getValueTooltip(utils, predicate.entityStruck()).build("ali.property.branch.stuck_entity"))
                )
                .build("ali.type.entity_sub_predicate.lightning_bolt");
    }

    @NotNull
    public static TooltipNode getFishingHookPredicateTooltip(IServerUtils utils, FishingHookPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, predicate.inOpenWater()).build("ali.property.value.in_open_water"))
                )
                .showEmpty()
                .build("ali.type.entity_sub_predicate.fishing_hook");
    }

    @NotNull
    public static TooltipNode getPlayerPredicateTooltip(IServerUtils utils, PlayerPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, predicate.level()).build("ali.property.value.level"))
                        .add(utils.getValueTooltip(utils, predicate.gameType()).build("ali.property.branch.game_types"))
                        .add(GenericTooltipUtils.getCollectionTooltip(utils, predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip).build("ali.property.branch.stats"))
                        .add(GenericTooltipUtils.getMapTooltip(utils, predicate.recipes(), GenericTooltipUtils::getRecipeEntryTooltip).build("ali.property.branch.recipes"))
                        .add(GenericTooltipUtils.getMapTooltip(utils, predicate.advancements(), GenericTooltipUtils::getAdvancementEntryTooltip).build("ali.property.branch.advancements"))
                        .add(utils.getValueTooltip(utils, predicate.lookingAt()).build("ali.property.branch.looking_at"))
                )
                .build("ali.type.entity_sub_predicate.player");
    }

    @NotNull
    public static TooltipNode getSlimePredicateTooltip(IServerUtils utils, SlimePredicate predicate) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, predicate.size()).build("ali.property.value.size"))
                )
                .build("ali.type.entity_sub_predicate.slime");
    }

    @NotNull
    public static TooltipNode getRaiderPredicateTooltip(IServerUtils utils, RaiderPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, predicate.hasRaid()).build("ali.property.value.has_raid"))
                        .add(utils.getValueTooltip(utils, predicate.isCaptain()).build("ali.property.value.is_captain"))
                )
                .build("ali.type.entity_sub_predicate.raider");
    }

    @NotNull
    public static <V> TooltipNode getVariantPredicateTooltip(IServerUtils utils, EntitySubPredicates.EntityVariantPredicateType<V>.Instance predicate) {
        TooltipBuilder tooltip = RegistriesTooltipUtils.getEntitySubPredicateTooltip(utils, predicate);

        if (predicate.variant instanceof Enum<?> variant) {
            tooltip.add(utils.getValueTooltip(utils, variant).build("ali.property.value.variant"));
        }

        return tooltip.build("ali.property.value.type");
    }

    @NotNull
    public static <V> TooltipNode getHolderVariantPredicateTooltip(IServerUtils utils, EntitySubPredicates.EntityHolderVariantPredicateType<V>.Instance predicate) {
        return RegistriesTooltipUtils.getEntitySubPredicateTooltip(utils, predicate)
                .add(utils.getValueTooltip(utils, predicate.variants).build("ali.property.branch.variants"))
                .build("ali.property.value.type");
    }
}
