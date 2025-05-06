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
        components.addAll(getRecipesTooltip(utils, pad + 1, "ali.property.branch.recipes", predicate.recipes()));
        components.addAll(getAdvancementsTooltip(utils, pad + 1, "ali.property.branch.advancements", predicate.advancements()));
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
        components.addAll(getHolderSetTooltip(utils, pad + 1, "ali.property.branch.variants", "ali.property.value.variant", predicate.variants, (u, i, s, v) -> switch (v) {
            case CatVariant catVariant -> getBuiltInRegistryTooltip(u, i, s, BuiltInRegistries.CAT_VARIANT, catVariant);
            case PaintingVariant paintingVariant -> getRegistryTooltip(u, i, s, Registries.PAINTING_VARIANT, paintingVariant);
            case FrogVariant frogVariant -> getBuiltInRegistryTooltip(u, i, s, BuiltInRegistries.FROG_VARIANT, frogVariant);
            case WolfVariant wolfVariant -> getRegistryTooltip(u, i, s, Registries.WOLF_VARIANT, wolfVariant);
            default -> List.of();
        }));

        return components;
    }
}
