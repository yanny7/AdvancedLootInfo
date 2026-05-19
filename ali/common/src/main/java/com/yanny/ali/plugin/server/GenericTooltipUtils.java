package com.yanny.ali.plugin.server;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.IMultiKey;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
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
    public static <T> TooltipBuilder getStandaloneTooltip(IServerUtils utils, Optional<ListOperation.StandAlone<T>> standalone) {
        return standalone.map((s) -> TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, s.value()).build(Lang.Branch.VALUES));
            b.add(utils.getValueTooltip(utils, s.operation()).build(Lang.Value.LIST_OPERATION));
        })).orElseGet(TooltipBuilder::empty);

    }

    @NotNull
    public static <T> TooltipBuilder getFilterableTooltip(IServerUtils utils, IMultiKey value, Collection<Filterable<T>> data) {
        return TooltipBuilder.array((b) -> {
            for (Filterable<T> d : data) {
                b.add(TooltipBuilder.array((c) -> {
                    c.add(utils.getValueTooltip(utils, d.raw()).build(Lang.Value.RAW));
                    c.add(utils.getValueTooltip(utils, d.filtered()).build(Lang.Value.FILTERED));
                }).key(value));
            }
        });
    }

    // MAP ENTRY

    @NotNull
    public static TooltipBuilder getRecipeEntryTooltip(IServerUtils ignoredUtils, Map.Entry<ResourceKey<Recipe<?>>, Boolean> entry) {
        return TooltipBuilder.keyValue(entry.getKey().location().toString(), entry.getValue());
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
    public static TooltipBuilder getDataComponentEntryTooltip(IServerUtils utils, Map.Entry<DataComponentType<?>, Boolean> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build(Lang.Value.VALUE));
    }

    @NotNull
    public static TooltipBuilder getAdvancementEntryTooltip(IServerUtils utils, Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()));
    }

    @NotNull
    public static TooltipBuilder getMapDecorationEntryTooltip(IServerUtils utils, Map.Entry<String, MapDecorations.Entry> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()))
                .key(Lang.Value.DECORATION);
    }

    @NotNull
    public static TooltipBuilder getBlockPropertyEntryTooltip(IServerUtils utils, Map.Entry<Holder<Block>, Property<?>> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build(Lang.Value.PROPERTY))
                .key(Lang.Value.BLOCK);
    }

    @NotNull
    public static TooltipBuilder getPropertiesEntryTooltip(IServerUtils utils, Map.Entry<String, Collection<com.mojang.authlib.properties.Property>> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build(Lang.Branch.PROPERTIES));
    }

    @NotNull
    public static TooltipBuilder getEnchantmentLevelEntryTooltip(IServerUtils utils, Map.Entry<Holder<Enchantment>, Integer> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build(Lang.Value.LEVEL));
    }

    @NotNull
    public static TooltipBuilder getDataComponentPatchEntryTooltip(IServerUtils utils, Map.Entry<DataComponentType<?>, Optional<?>> entry) {
        TooltipBuilder builder = utils.getValueTooltip(utils, entry.getKey());

        entry.getValue().ifPresent((v) -> builder.add(utils.getDataComponentTypeTooltip(utils, entry.getKey(), v)));

        if (entry.getValue().isEmpty()) {
            builder.add(TooltipBuilder.keyOnly(CoreLang.Utils.REMOVED));
        }

        return builder;
    }

    @NotNull
    public static TooltipBuilder getDataComponentPredicateEntryTooltip(IServerUtils utils, Map.Entry<DataComponentPredicate.Type<?>, DataComponentPredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getDataComponentPredicateTooltip(utils, entry.getValue()));
    }

    @NotNull
    public static TooltipBuilder getSlotRangePredicateEntryTooltip(IServerUtils utils, Map.Entry<SlotRange, ItemPredicate> entry) {
        return TooltipBuilder.keyValue(entry.getKey().toString(), entry.getKey().slots().toString())
                .add(utils.getValueTooltip(utils, entry.getValue()).build(Lang.Branch.PREDICATE));
    }

    @NotNull
    public static <V, T extends Registry<V>> TooltipBuilder getRegistryTooltip(IServerUtils utils, ResourceKey<T> registry, V value) {
        HolderLookup.Provider provider = utils.lookupProvider();

        if (provider != null) {
            Optional<? extends HolderLookup.RegistryLookup<V>> lookup = provider.lookup(registry);

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
    public static <A, B extends Predicate<A>> TooltipBuilder getCollectionContentsPredicateTooltip(IServerUtils utils, Optional<CollectionContentsPredicate<A, B>> predicate) {
        return predicate.map((p) -> utils.getValueTooltip(utils, p.unpack())).orElse(TooltipBuilder.empty());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <A, B extends Predicate<A>> TooltipBuilder getCollectionCountsPredicateTooltip(IServerUtils utils, Optional<CollectionCountsPredicate<A, B>> predicate) {
        return predicate.map((p) -> utils.getValueTooltip(utils, p.unpack())).orElse(TooltipBuilder.empty());
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
    public static String getStatTranslationKey(ResourceLocation location) {
        return "stat." + location.toString().replace(':', '.');
    }
}
