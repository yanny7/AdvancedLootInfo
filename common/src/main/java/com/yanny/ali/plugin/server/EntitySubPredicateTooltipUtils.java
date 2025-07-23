package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.advancements.critereon.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class EntitySubPredicateTooltipUtils {
    @NotNull
    public static ITooltipNode getLightningBoltPredicateTooltip(IServerUtils utils, LightningBoltPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.entity_sub_predicate.lightning_bolt"));
        
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.blocks_on_fire", predicate.blocksSetOnFire()));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.stuck_entity", predicate.entityStruck(), GenericTooltipUtils::getEntityPredicateTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getFishingHookPredicateTooltip(IServerUtils utils, FishingHookPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.entity_sub_predicate.fishing_hook"));
        
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.in_open_water", predicate.inOpenWater(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getPlayerPredicateTooltip(IServerUtils utils, PlayerPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.entity_sub_predicate.player"));
        
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.level", predicate.level()));
        tooltip.add(getGameTypePredicateTooltip(utils, "ali.property.branch.game_types", predicate.gameType()));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.stats", predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip));
        tooltip.add(getMapTooltip(utils, "ali.property.branch.recipes", predicate.recipes(), GenericTooltipUtils::getRecipeEntryTooltip));
        tooltip.add(getMapTooltip(utils, "ali.property.branch.advancements", predicate.advancements(), GenericTooltipUtils::getAdvancementEntryTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.looking_at", predicate.lookingAt(), GenericTooltipUtils::getEntityPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.input", predicate.input(), GenericTooltipUtils::getInputPredicateTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSlimePredicateTooltip(IServerUtils utils, SlimePredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.entity_sub_predicate.slime"));
        
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.size", predicate.size()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getRaiderPredicateTooltip(IServerUtils utils, RaiderPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.entity_sub_predicate.raider"));
        
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.has_raid", predicate.hasRaid()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.is_captain", predicate.isCaptain()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSheepPredicateTooltip(IServerUtils utils, SheepPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.entity_sub_predicate.sheep"));

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.sheared", predicate.sheared(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }
}
