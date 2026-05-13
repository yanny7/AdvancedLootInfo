package com.yanny.ali.plugin.server;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.ToggleTooltips;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class GenericTooltipUtils {
    @NotNull
    public static TooltipBuilder getConditionsSectionTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        if (!conditions.isEmpty()) {
            return TooltipBuilder.array((b) -> {
                b.add(TooltipBuilder.keyOnly("ali.util.advanced_loot_info.delimiter.conditions"));
                b.add(utils.getValueTooltip(utils, conditions));
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getFunctionsSectionTooltip(IServerUtils utils, List<LootItemFunction> functions) {
        if (!functions.isEmpty()) {
            return TooltipBuilder.array((b) -> {
                b.add(TooltipBuilder.keyOnly("ali.util.advanced_loot_info.delimiter.functions"));
                b.add(utils.getValueTooltip(utils, functions));
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static <K, V> TooltipBuilder getMapTooltip(IServerUtils utils, Map<K, V> values, BiFunction<IServerUtils, Map.Entry<K, V>, TooltipBuilder> mapper) {
        if (!values.isEmpty()) {
            return TooltipBuilder.branch((b) -> values.entrySet().forEach((e) -> b.add(mapper.apply(utils, e))));
        }

        return TooltipBuilder.empty();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <T> TooltipBuilder getStandaloneTooltip(IServerUtils utils, String value, Optional<ListOperation.StandAlone<T>> standalone) {
        return standalone.map((s) -> TooltipBuilder.array((b) -> {
            b.add(getCollectionTooltip(utils, value, s.value()).build("aci.util.values"));
            b.add(utils.getValueTooltip(utils, s.operation()).build("ali.property.value.list_operation"));
        })).orElseGet(TooltipBuilder::empty);

    }

    @NotNull
    public static <T> TooltipBuilder getFilterableTooltip(IServerUtils utils, String value, Collection<Filterable<T>> data) {
        return TooltipBuilder.array((b) -> {
            for (Filterable<T> d : data) {
                b.add(TooltipBuilder.array((c) -> {
                    c.add(utils.getValueTooltip(utils, d.raw()).build("ali.property.value.raw"));
                    c.add(utils.getValueTooltip(utils, d.filtered()).build("ali.property.value.filtered"));
                }).key(value));
            }
        });
    }

    // MAP ENTRY

    @NotNull
    public static TooltipBuilder getStatsEntryTooltip(IServerUtils utils, Map.Entry<Stat<?>, MinMaxBounds.Ints> entry) {
        Stat<?> stat = entry.getKey();
        MinMaxBounds.Ints ints = entry.getValue();
        Object value = stat.getValue();

        if (value instanceof Item item) {
            TooltipBuilder itemTooltip = utils.getValueTooltip(utils, item);

            itemTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(stat.getType().getTranslationKey()), toString(ints)).build());
            return itemTooltip.key(Lang.Value.ITEM);
        } else if (value instanceof Block block) {
            TooltipBuilder blockTooltip = utils.getValueTooltip(utils, block);

            blockTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(stat.getType().getTranslationKey()), toString(ints)).build());
            return blockTooltip.key(Lang.Value.BLOCK);
        } else if (value instanceof EntityType<?> entityType) {
            TooltipBuilder entityTooltip = utils.getValueTooltip(utils, entityType);

            entityTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(stat.getType().getTranslationKey()), toString(ints)).build());
            return entityTooltip.key(Lang.Value.ENTITY_TYPE);
        } else if (value instanceof ResourceLocation resourceLocation) {
            TooltipBuilder locationTooltip = utils.getValueTooltip(utils, resourceLocation);

            locationTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(getTranslationKey(resourceLocation)), toString(ints)).build());
            return locationTooltip.key(Lang.Value.ID);
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getRecipeEntryTooltip(IServerUtils ignoredUtils, Map.Entry<ResourceLocation, Boolean> entry) {
        return TooltipBuilder.keyValue(entry.getKey().toString(), entry.getValue());
    }

    @NotNull
    public static TooltipBuilder getCriterionEntryTooltip(IServerUtils ignoredUtils, Map.Entry<String, Boolean> entry) {
        return TooltipBuilder.keyValue(entry.getKey(), entry.getValue());
    }

    @NotNull
    public static TooltipBuilder getStringEntryTooltip(IServerUtils ignoredUtils, Map.Entry<String, String> entry) {
        return TooltipBuilder.keyValue(entry.getKey(), entry.getValue());
    }

    @NotNull
    public static TooltipBuilder getIntRangeEntryTooltip(IServerUtils utils, Map.Entry<String, IntRange> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build(Lang.Value.LIMIT));
    }

    @NotNull
    public static TooltipBuilder getMobEffectPredicateEntryTooltip(IServerUtils utils, Map.Entry<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue().amplifier()).build(Lang.Value.AMPLIFIER))
                .add(utils.getValueTooltip(utils, entry.getValue().duration()).build(Lang.Value.DURATION))
                .add(utils.getValueTooltip(utils, entry.getValue().ambient()).build(Lang.Value.IS_AMBIENT))
                .add(utils.getValueTooltip(utils, entry.getValue().visible()).build(Lang.Value.IS_VISIBLE));
    }

    @NotNull
    public static TooltipBuilder getEnchantmentLevelsEntryTooltip(IServerUtils utils, Map.Entry<Holder<Enchantment>, NumberProvider> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build(Lang.Value.LEVELS));
    }

    @NotNull
    public static TooltipBuilder getAdvancementEntryTooltip(IServerUtils utils, Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()));
    }

    @NotNull
    public static TooltipBuilder getMapDecorationEntryTooltip(IServerUtils utils, Map.Entry<String, MapDecorations.Entry> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("aci.util.null"))
                .key("ali.property.value.decoration");
    }

    @NotNull
    public static TooltipBuilder getBlockPropertyEntryTooltip(IServerUtils utils, Map.Entry<Holder<Block>, Property<?>> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.property"))
                .key("ali.property.value.block");
    }

    @NotNull
    public static TooltipBuilder getPropertiesEntryTooltip(IServerUtils utils, Map.Entry<String, Collection<com.mojang.authlib.properties.Property>> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(GenericTooltipUtils.getCollectionTooltip(utils, "ali.property.branch.property", entry.getValue()).build("ali.property.branch.properties"));
    }

    @NotNull
    public static TooltipBuilder getEnchantmentLevelEntryTooltip(IServerUtils utils, Map.Entry<Holder<Enchantment>, Integer> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.level"));
    }

    @NotNull
    public static TooltipBuilder getToggleEntryTooltip(IServerUtils utils, Map.Entry<ToggleTooltips.ComponentToggle<?>, Boolean> entry) {
        return getDataComponentTypeTooltip(utils, entry.getKey().type())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.value"));
    }

    @NotNull
    public static TooltipBuilder getDataComponentPatchEntryTooltip(IServerUtils utils, Map.Entry<DataComponentType<?>, Optional<?>> entry) {
        TooltipBuilder builder = getDataComponentTypeTooltip(utils, entry.getKey());

        entry.getValue().ifPresent((v) -> builder.add(utils.getDataComponentTypeTooltip(utils, entry.getKey(), v)));

        if (entry.getValue().isEmpty()) {
            builder.add(TooltipBuilder.keyOnly("aci.util.removed"));
        }

        return builder;
    }

    @NotNull
    public static TooltipBuilder getItemSubPredicateEntryTooltip(IServerUtils utils, Map.Entry<ItemSubPredicate.Type<?>, ItemSubPredicate> entry) {
        return TooltipBuilder.array((b) -> b.add(utils.getItemSubPredicateTooltip(utils, entry.getValue())));
    }

    @NotNull
    public static TooltipBuilder getSlotRangePredicateEntryTooltip(IServerUtils utils, Map.Entry<SlotRange, ItemPredicate> entry) {
        return TooltipBuilder.keyValue(entry.getKey().toString(), entry.getKey().slots().toString())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.branch.predicate"));
    }

    @NotNull
    public static <V, T extends Registry<V>> TooltipBuilder getRegistryTooltip(IServerUtils utils, ResourceKey<T> registry, V value) {
        HolderLookup.Provider provider = utils.lookupProvider();

        if (provider != null) {
            Optional<HolderLookup.RegistryLookup<V>> lookup = provider.lookup(registry);

            if (lookup.isPresent()) {
                Optional<Holder.Reference<V>> first = lookup.get().listElements().filter((l) -> l.value() == value).findFirst();

                if (first.isPresent()) {
                    return utils.getValueTooltip(utils, Objects.requireNonNull(first.get().key()));
                }
            }
        }

        return TooltipBuilder.empty();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <A, B extends Predicate<A>> TooltipBuilder getCollectionPredicateTooltip(IServerUtils utils, String value, Optional<CollectionPredicate<A, B>> optional) {
        if (optional.isPresent()) {
            CollectionPredicate<A, B> predicate = optional.get();

            return TooltipBuilder.array((b) -> b
                    .add(getCollectionContentsPredicateTooltip(utils, value, predicate.contains()).build("ali.property.branch.contains"))
                    .add(getCollectionCountsPredicateTooltip(utils, value, predicate.counts()).build("ali.property.branch.counts"))
                    .add(utils.getValueTooltip(utils, predicate.size()).build("ali.property.value.size"))
            );
        }

        return TooltipBuilder.empty();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <A, B extends Predicate<A>> TooltipBuilder getCollectionContentsPredicateTooltip(IServerUtils utils, String value, Optional<CollectionContentsPredicate<A, B>> predicate) {
        return predicate.map((p) -> getCollectionTooltip(utils, value, p.unpack())).orElse(TooltipBuilder.empty());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <A, B extends Predicate<A>> TooltipBuilder getCollectionCountsPredicateTooltip(IServerUtils utils, String value, Optional<CollectionCountsPredicate<A, B>> predicate) {
        return predicate.map((p) -> getCollectionTooltip(utils, value, p.unpack())).orElse(TooltipBuilder.empty());
    }

    @NotNull
    public static String toString(MinMaxBounds.Doubles doubles) {
        Optional<Double> min = doubles.min();
        Optional<Double> max = doubles.max();

        if (min.isPresent()) {
            if (max.isPresent()) {
                if (!Objects.equals(min.get(), max.get())) {
                    return String.format("%.1f-%.1f", min.get(), max.get());
                } else {
                    return String.format("=%.1f", min.get());
                }
            } else {
                return String.format("≥%.1f", min.get());
            }
        } else {
            return max.map(aDouble -> String.format("≤%.1f", aDouble)).orElse("???");
        }
    }

    @NotNull
    public static String toString(MinMaxBounds.Ints ints) {
        Optional<Integer> min = ints.min();
        Optional<Integer> max = ints.max();

        if (min.isPresent()) {
            if (max.isPresent()) {
                if (!Objects.equals(min.get(), max.get())) {
                    return String.format("%d-%d", min.get(), max.get());
                } else {
                    return String.format("=%d", min.get());
                }
            } else {
                return String.format("≥%d", min.get());
            }
        } else {
            return max.map(integer -> String.format("≤%d", integer)).orElse("???");
        }
    }

    @NotNull
    private static String getTranslationKey(ResourceLocation location) {
        return "stat." + location.toString().replace(':', '.');
    }
}
