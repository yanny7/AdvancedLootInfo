package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;

public class EntitySubPredicateTooltipUtils {
    @NotNull
    public static List<Component> getLightningBoltPredicateTooltip(IClientUtils utils, int pad, LightningBoltPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.lightning_bolt")));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.blocks_on_fire", predicate.blocksSetOnFire()));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.stuck_entity", predicate.entityStruck(), GenericTooltipUtils::getEntityPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getFishingHookPredicateTooltip(IClientUtils utils, int pad, FishingHookPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.fishing_hook")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.in_open_water", predicate.inOpenWater(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getPlayerPredicateTooltip(IClientUtils utils, int pad, PlayerPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.player")));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", predicate.level()));
        components.addAll(getGameTypePredicateTooltip(utils, pad + 1, "ali.property.branch.game_types", predicate.gameType()));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.stats", predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip));
        components.addAll(getMapTooltip(utils, pad + 1, "ali.property.branch.recipes", predicate.recipes(), GenericTooltipUtils::getRecipeEntryTooltip));
        components.addAll(getMapTooltip(utils, pad + 1, "ali.property.branch.advancements", predicate.advancements(), GenericTooltipUtils::getAdvancementEntryTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.looking_at", predicate.lookingAt(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.input", predicate.input(), GenericTooltipUtils::getInputPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSlimePredicateTooltip(IClientUtils utils, int pad, SlimePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.slime")));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.size", predicate.size()));

        return components;
    }

    @NotNull
    public static List<Component> getRaiderPredicateTooltip(IClientUtils utils, int pad, RaiderPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.raider")));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.has_raid", predicate.hasRaid()));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.is_captain", predicate.isCaptain()));

        return components;
    }

    @NotNull
    public static List<Component> getSheepPredicateTooltip(IClientUtils utils, int pad, SheepPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.sheep")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.sheared", predicate.sheared(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }
}
