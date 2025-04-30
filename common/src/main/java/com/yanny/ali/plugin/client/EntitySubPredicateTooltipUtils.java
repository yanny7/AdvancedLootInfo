package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
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
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.stuck_entity", predicate.entityStruck(), GenericTooltipUtils::getEntityPredicateTooltip));

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
        components.addAll(getGameTypeTooltip(utils, pad + 1, predicate.gameType()));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.stats", predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip));
        components.addAll(getRecipesTooltip(utils, pad + 1, predicate.recipes()));
        components.addAll(getAdvancementsTooltip(utils, pad + 1, predicate.advancements()));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.looking_at", predicate.lookingAt(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, predicate.input(), GenericTooltipUtils::getInputPredicateTooltip));

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
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.color", predicate.color(), GenericTooltipUtils::getEnumTooltip));

        return components;
    }

    @NotNull
    public static <V> List<Component> getVariantPredicateTooltip(IClientUtils utils, int pad, EntitySubPredicates.EntityVariantPredicateType<V>.Instance predicate) {
        List<Component> components = new LinkedList<>(getBuiltInRegistryTooltip(utils, pad, "ali.property.value.type", BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, predicate.codec()));

        if (predicate.variant instanceof Enum<?> variant) {
            components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.variant", variant));
        }

        return components;
    }

    @NotNull
    public static <V> List<Component> getHolderVariantPredicateTooltip(IClientUtils utils, int pad, EntitySubPredicates.EntityHolderVariantPredicateType<V>.Instance predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getBuiltInRegistryTooltip(utils, pad, "ali.property.value.type", BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, predicate.codec()));
        components.addAll(getHolderSetTooltip(utils, pad + 1, "ali.property.branch.variants", predicate.variants, (u, i, v) -> {
            if (v instanceof CatVariant catVariant) {
                return getBuiltInRegistryTooltip(u, i, "ali.property.value.variant", BuiltInRegistries.CAT_VARIANT, catVariant);
            } else if (v instanceof PaintingVariant paintingVariant) {
                return getRegistryTooltip(u, i, "ali.property.value.variant", Registries.PAINTING_VARIANT, paintingVariant);
            } else if (v instanceof FrogVariant frogVariant) {
                return getBuiltInRegistryTooltip(u, i, "ali.property.value.variant", BuiltInRegistries.FROG_VARIANT, frogVariant);
            } else if (v instanceof WolfVariant wolfVariant) {
                return getRegistryTooltip(u, i, "ali.property.value.variant", Registries.WOLF_VARIANT, wolfVariant);
            }

            return List.of();
        }));

        return components;
    }
}
