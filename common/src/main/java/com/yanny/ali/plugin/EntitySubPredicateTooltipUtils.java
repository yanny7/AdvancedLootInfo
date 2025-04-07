package com.yanny.ali.plugin;

import com.yanny.ali.api.IUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.yanny.ali.plugin.GenericTooltipUtils.*;

public class EntitySubPredicateTooltipUtils {
    @NotNull
    public static List<Component> getLightningBoltPredicateTooltip(IUtils utils, int pad, LightningBoltPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.lightning_bolt")));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.blocks_on_fire", predicate.blocksSetOnFire()));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.stuck_entity", predicate.entityStruck(), GenericTooltipUtils::getEntityPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getFishingHookPredicateTooltip(IUtils utils, int pad, FishingHookPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.fishing_hook")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.in_open_water", predicate.inOpenWater(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getPlayerPredicateTooltip(IUtils utils, int pad, PlayerPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.player")));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", predicate.level()));
        components.addAll(getOptionalTooltip(utils, pad + 1, predicate.gameType(), GenericTooltipUtils::getGameTypeTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.stats", predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip));
        components.addAll(getRecipesTooltip(utils, pad + 1, predicate.recipes()));
        components.addAll(getAdvancementsTooltip(utils, pad + 1, predicate.advancements()));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.looking_at", predicate.lookingAt(),
                (u, p, s, c) -> getComponentsTooltip(u, p, s, c, GenericTooltipUtils::getEntityPredicateTooltip)));

        return components;
    }

    @NotNull
    public static List<Component> getSlimePredicateTooltip(IUtils utils, int pad, SlimePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.slime")));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.common.size", predicate.size()));

        return components;
    }

    @NotNull
    public static List<Component> getRaiderPredicateTooltip(IUtils utils, int pad, RaiderPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.raider")));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.common.has_raid", predicate.hasRaid()));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.common.is_captain", predicate.isCaptain()));

        return components;
    }

    @NotNull
    public static <V> List<Component> getVariantPredicateTooltip(IUtils utils, int pad, EntitySubPredicates.EntityVariantPredicateType<V>.Instance predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.variant")));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.type", Objects.requireNonNull(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE.getKey(predicate.codec()))));

        if (predicate.variant instanceof Enum<?> variant) {
            components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.variant", variant));
        }

        return components;
    }

    @NotNull
    public static <V> List<Component> getHolderVariantPredicateTooltip(IUtils utils, int pad, EntitySubPredicates.EntityHolderVariantPredicateType<V>.Instance predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.entity_sub_predicate.variant")));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.type", Objects.requireNonNull(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE.getKey(predicate.codec()))));
        components.addAll(getHolderSetTooltip(utils, pad + 1, "ali.property.branch.variants", predicate.variants,
                (u, i, v) -> List.of()));

        return components;
    }
}
