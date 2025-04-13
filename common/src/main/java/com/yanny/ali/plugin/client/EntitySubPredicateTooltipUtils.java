package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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
import java.util.Objects;
import java.util.Optional;

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
        List<Component> components = new LinkedList<>(getResourceLocationTooltip(utils, pad, "ali.property.value.type", Objects.requireNonNull(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE.getKey(predicate.codec()))));

        if (predicate.variant instanceof Enum<?> variant) {
            components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.variant", variant));
        }

        return components;
    }

    @NotNull
    public static <V> List<Component> getHolderVariantPredicateTooltip(IClientUtils utils, int pad, EntitySubPredicates.EntityHolderVariantPredicateType<V>.Instance predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getResourceLocationTooltip(utils, pad, "ali.property.value.type", Objects.requireNonNull(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE.getKey(predicate.codec()))));
        components.addAll(getHolderSetTooltip(utils, pad + 1, "ali.property.branch.variants", predicate.variants, (u, i, v) -> {
            if (v instanceof CatVariant catVariant) {
                return getResourceLocationTooltip(u, i, "ali.property.value.variant", Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.getKey(catVariant)));
            } else if (v instanceof PaintingVariant paintingVariant) {
                HolderLookup.Provider provider = utils.lookupProvider();

                if (provider != null) {
                    Optional<HolderLookup.RegistryLookup<PaintingVariant>> lookup = provider.lookup(Registries.PAINTING_VARIANT);

                    if (lookup.isPresent()) {
                        Optional<Holder.Reference<PaintingVariant>> first = lookup.get().listElements().filter((l) -> l.value() == paintingVariant).findFirst();

                        if (first.isPresent()) {
                            return getResourceKeyTooltip(u, i, "ali.property.value.variant", Objects.requireNonNull(first.get().key()));
                        }
                    }
                }
            } else if (v instanceof FrogVariant frogVariant) {
                return getResourceLocationTooltip(u, i, "ali.property.value.variant", Objects.requireNonNull(BuiltInRegistries.FROG_VARIANT.getKey(frogVariant)));
            } else if (v instanceof WolfVariant wolfVariant) {
                HolderLookup.Provider provider = utils.lookupProvider();

                if (provider != null) {
                    Optional<HolderLookup.RegistryLookup<WolfVariant>> lookup = provider.lookup(Registries.WOLF_VARIANT);

                    if (lookup.isPresent()) {
                        Optional<Holder.Reference<WolfVariant>> first = lookup.get().listElements().filter((l) -> l.value() == wolfVariant).findFirst();

                        if (first.isPresent()) {
                            return getResourceKeyTooltip(u, i, "ali.property.value.variant", Objects.requireNonNull(first.get().key()));
                        }
                    }
                }
            }

            return List.of();
        }));

        return components;
    }
}
