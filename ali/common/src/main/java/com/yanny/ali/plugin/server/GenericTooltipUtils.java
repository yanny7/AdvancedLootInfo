package com.yanny.ali.plugin.server;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.slot.SlotSource;
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

public class GenericTooltipUtils {
    @NotNull
    public static TooltipBuilder getConditionsSectionTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        if (!conditions.isEmpty()) {
            return getSectionTooltip(utils.getValueTooltip(utils, conditions).build(), "ali.util.advanced_loot_info.delimiter.conditions");
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getFunctionsSectionTooltip(IServerUtils utils, List<LootItemFunction> functions) {
        if (!functions.isEmpty()) {
            return getSectionTooltip(utils.getValueTooltip(utils, functions).build(), "ali.util.advanced_loot_info.delimiter.functions");
        }

        return TooltipBuilder.empty();
    }

    /**
     * Prepends a delimiter line to a section. The delimiter is a sibling of the content, so it has to mirror the
     * content's visibility itself - otherwise a section whose entries are all advanced-only would render as a lone
     * delimiter with nothing under it.
     */
    @NotNull
    private static TooltipBuilder getSectionTooltip(TooltipNode content, String delimiterKey) {
        if (content.isBlank(true)) {
            return TooltipBuilder.empty();
        }

        TooltipBuilder delimiter = TooltipBuilder.keyOnly(delimiterKey);

        if (content.isBlank(false)) {
            delimiter.isAdvancedTooltip();
        }

        return TooltipBuilder.array((b) -> {
            b.add(delimiter);
            b.add(content);
        });
    }

    @NotNull
    public static TooltipBuilder getSlotListTooltip(IServerUtils utils, List<SlotSource> slots) {
        return TooltipBuilder.array((b) -> {
            for (SlotSource slot : slots) {
                b.add(utils.getSlotSourceTooltip(utils, slot));
            }
        });
    }

    @NotNull
    public static <K, V> TooltipBuilder getMapTooltip(IServerUtils utils, Map<K, V> values, Comparator<K> comparator, BiFunction<IServerUtils, Map.Entry<K, V>, TooltipBuilder> mapper) {
        if (!values.isEmpty()) {
            return TooltipBuilder.branch((b) -> values.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(comparator))
                    .forEach((e) -> b.add(mapper.apply(utils, e))));
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

    // MAP ENTRY

    @NotNull
    public static TooltipBuilder getRecipeEntryTooltip(IServerUtils ignoredUtils, Map.Entry<ResourceKey<Recipe<?>>, Boolean> entry) {
        return TooltipBuilder.keyValue(entry.getKey().identifier().toString(), entry.getValue());
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
    public static TooltipBuilder getAdvancementEntryTooltip(IServerUtils utils, Map.Entry<Identifier, PlayerPredicate.AdvancementPredicate> entry) {
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
                .add(utils.getValueTooltip(utils, entry.getValue()));
    }

    @NotNull
    public static TooltipBuilder getSlotRangePredicateEntryTooltip(IServerUtils utils, Map.Entry<SlotRange, ItemPredicate> entry) {
        return TooltipBuilder.keyValue(entry.getKey().toString(), entry.getKey().slots().toString())
                .add(utils.getValueTooltip(utils, entry.getValue()).build(Lang.Branch.PREDICATE));
    }

    @NotNull
    public static <V, T extends Registry<V>> TooltipBuilder getRegistryTooltip(IServerUtils utils, ResourceKey<T> registry, V value) {
        HolderLookup.Provider provider = utils.lookupProvider();
        Optional<? extends HolderLookup.RegistryLookup<V>> lookup = provider.lookup(registry);

        if (lookup.isPresent()) {
            Optional<Holder.Reference<V>> first = lookup.get().listElements().filter((l) -> l.value() == value).findFirst();

            if (first.isPresent()) {
                return utils.getValueTooltip(utils, Objects.requireNonNull(first.get().key()));
            }
        }

        return TooltipBuilder.empty();
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
    public static String getTranslationKey(Identifier location) {
        return "stat." + location.toString().replace(':', '.');
    }
}
