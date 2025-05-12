package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;
import static com.yanny.ali.plugin.client.RegistriesTooltipUtils.*;

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
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.game_type", predicate.gameType(), GenericTooltipUtils::getEnumTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.stats", predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip));
        components.addAll(getMapTooltip(utils, pad + 1, "ali.property.branch.recipes", predicate.recipes(), GenericTooltipUtils::getRecipeEntryTooltip));
        components.addAll(getMapTooltip(utils, pad + 1, "ali.property.branch.advancements", predicate.advancements(), GenericTooltipUtils::getAdvancementEntryTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.looking_at", predicate.lookingAt(), GenericTooltipUtils::getEntityPredicateTooltip));

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
    public static <V> List<Component> getVariantPredicateTooltip(IClientUtils utils, int pad, EntitySubPredicates.EntityVariantPredicateType<V>.Instance predicate) {
        List<Component> components = new LinkedList<>(RegistriesTooltipUtils.getEntitySubPredicateTooltip(utils, pad, "ali.property.value.type", predicate));

        if (predicate.variant instanceof Enum<?> variant) {
            components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.variant", variant));
        }

        return components;
    }

    @NotNull
    public static <V> List<Component> getHolderVariantPredicateTooltip(IClientUtils utils, int pad, EntitySubPredicates.EntityHolderVariantPredicateType<V>.Instance predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(RegistriesTooltipUtils.getEntitySubPredicateTooltip(utils, pad, "ali.property.value.type", predicate));
        components.addAll(getHolderSetTooltip(utils, pad + 1, "ali.property.branch.variants", "ali.property.value.variant", predicate.variants, (u, i, s, v) -> switch (v) {
            case CatVariant catVariant -> getCatVariantTooltip(u, i, s, catVariant);
            case PaintingVariant paintingVariant -> getPaintingVariantTooltip(u, i, s, paintingVariant);
            case FrogVariant frogVariant -> getFrogVariantTooltip(u, i, s, frogVariant);
            case WolfVariant wolfVariant -> getWolfVariantTooltip(u, i, s, wolfVariant);
            default -> List.of();
        }));

        return components;
    }
}
