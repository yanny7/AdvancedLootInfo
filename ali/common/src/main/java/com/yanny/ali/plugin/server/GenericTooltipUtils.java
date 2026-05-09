package com.yanny.ali.plugin.server;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.*;

public class GenericTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static TooltipNode getMissingEntryTooltip(IServerUtils utils, LootPoolEntryContainer entry) {
        TooltipBuilder tooltip = getEntryTypeTooltip(utils, entry.getType());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, Objects.requireNonNull(utils.lookupProvider()));
            //noinspection unchecked
            MapCodec<LootPoolEntryContainer> codec = ((MapCodec<LootPoolEntryContainer>) entry.getType().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, entry).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get entry info from serialized data for {} in {}", BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.getKey(entry.getType()), utils.getCurrentLootTable(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, entry, CompositeEntryBase.class);
        }

        return tooltip.build("aci.util.auto_detected");
    }

    @NotNull
    public static TooltipNode getMissingFunctionTooltip(IServerUtils utils, LootItemFunction function) {
        TooltipBuilder tooltip = getFunctionTypeTooltip(utils, function.getType());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, Objects.requireNonNull(utils.lookupProvider()));
            //noinspection unchecked
            MapCodec<LootItemFunction> codec = ((MapCodec<LootItemFunction>) function.getType().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, function).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get function info from serialized data for {} in {}", BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(function.getType()), utils.getCurrentLootTable(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, function, LootItemFunction.class);
        }

        return tooltip.build("aci.util.auto_detected");
    }

    @NotNull
    public static TooltipNode getMissingConditionTooltip(IServerUtils utils, LootItemCondition condition) {
        TooltipBuilder tooltip = getConditionTypeTooltip(utils, condition.getType());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, Objects.requireNonNull(utils.lookupProvider()));
            //noinspection unchecked
            MapCodec<LootItemCondition> codec = ((MapCodec<LootItemCondition>) condition.getType().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, condition).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get condition info from serialized data for {} in {}", BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.getType()), utils.getCurrentLootTable(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, condition, LootItemCondition.class);
        }

        return tooltip.build("aci.util.auto_detected");
    }

    @NotNull
    public static TooltipNode getMissingIngredientTooltip(IServerUtils utils, Ingredient ingredient) {
        TooltipBuilder tooltip = TooltipBuilder.value(ingredient.getClass().getName());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, Objects.requireNonNull(utils.lookupProvider()));
            JsonElement jsonElement = Ingredient.CODEC.encodeStart(registryOps, ingredient).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get ingredient info from serialized data for {} in {}", ingredient.getClass().getName(), utils.getCurrentLootTable(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, ingredient, Ingredient.class);
        }

        return tooltip.build("aci.util.auto_detected");
    }

    public static TooltipNode getMissingDataComponentPredicateTooltip(IServerUtils utils, DataComponentPredicate predicate) {
        TooltipBuilder tooltip = TooltipBuilder.value(predicate.getClass().getName());

        TooltipUtils.addObjectFields(utils, tooltip, predicate, DataComponentPredicate.class);
        return tooltip.build("ali.util.advanced_loot_info.auto_detected");
    }

    public static TooltipNode getMissingEntitySubPredicateTooltip(IServerUtils utils, EntitySubPredicate predicate) {
        TooltipBuilder tooltip = getEntitySubPredicateTooltip(utils, predicate);

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, Objects.requireNonNull(utils.lookupProvider()));
            //noinspection unchecked
            MapCodec<EntitySubPredicate> codec = ((MapCodec<EntitySubPredicate>) predicate.codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, predicate).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get entity sub predicate info from serialized data for {} in {}", BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE.getKey(predicate.codec()), utils.getCurrentLootTable(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, predicate, EntitySubPredicate.class);
        }

        return tooltip.build("ali.util.advanced_loot_info.auto_detected");
    }

    public static TooltipNode getMissingDataComponentTypeTooltip(IServerUtils utils, DataComponentType<?> type, Object value) {
        TooltipBuilder tooltip = getDataComponentTypeTooltip(utils, type);

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, Objects.requireNonNull(utils.lookupProvider()));
            //noinspection unchecked
            Codec<Object> codec = ((Codec<Object>) type.codec());
            JsonElement jsonElement = Objects.requireNonNull(codec).encodeStart(registryOps, value).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get data component type info from serialized data for {} in {}", BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type), utils.getCurrentLootTable(), e);
            }
        }

        return tooltip.build("ali.util.advanced_loot_info.auto_detected");
    }

    @NotNull
    public static TooltipNode getMissingConsumableEffectTooltip(IServerUtils utils, ConsumeEffect effect) {
        TooltipBuilder tooltip = getConsumeEffectTypeTooltip(utils, effect.getType());

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, Objects.requireNonNull(utils.lookupProvider()));
            //noinspection unchecked
            MapCodec<ConsumeEffect> codec = ((MapCodec<ConsumeEffect>) effect.getType().codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, effect).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get consume effect info from serialized data for {} in {}", BuiltInRegistries.CONSUME_EFFECT_TYPE.getKey(effect.getType()), utils.getCurrentLootTable(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, effect, ConsumeEffect.class);
        }

        return tooltip.build("ali.util.advanced_loot_info.auto_detected");
    }

    @NotNull
    public static TooltipNode getMissingSlotSourceTooltip(IServerUtils utils, SlotSource slotSource) {
        TooltipBuilder tooltip = getSlotSourceTooltip(utils, slotSource);

        try {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, Objects.requireNonNull(utils.lookupProvider()));
            //noinspection unchecked
            MapCodec<SlotSource> codec = ((MapCodec<SlotSource>) slotSource.codec());
            JsonElement jsonElement = codec.codec().encodeStart(registryOps, slotSource).getPartialOrThrow();

            tooltip.add(TooltipUtils.getJsonTooltip(utils, jsonElement));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get consume effect info from serialized data for {} in {}", BuiltInRegistries.SLOT_SOURCE_TYPE.getKey(slotSource.codec()), utils.getCurrentLootTable(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, slotSource, SlotSource.class);
        }

        return tooltip.build("ali.util.advanced_loot_info.auto_detected");
    }

    @NotNull
    public static TooltipNode getMissingItemListingTooltip(IServerUtils utils, VillagerTrades.ItemListing itemListing) {
        TooltipBuilder tooltip = TooltipBuilder.value(itemListing.getClass().getName());

        TooltipUtils.addObjectFields(utils, tooltip, itemListing, VillagerTrades.ItemListing.class);
        return tooltip.build("aci.util.auto_detected");
    }

    @NotNull
    public static TooltipBuilder getMissingValueTooltip(IServerUtils ignoredUtils, Object object) {
        return TooltipBuilder.error("[" + object.getClass().getTypeName() + "]").key("aci.util.missing");
    }

    @NotNull
    public static TooltipBuilder getConditionListTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> {
            for (LootItemCondition condition : conditions) {
                b.add(utils.getConditionTooltip(utils, condition));
            }
        });
    }

    @NotNull
    public static TooltipBuilder getConditionsTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        if (!conditions.isEmpty()) {
            return TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly("ali.util.advanced_loot_info.delimiter.conditions"))
                            .add(getConditionListTooltip(utils, conditions))
                    );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getSubConditionsTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        if (!conditions.isEmpty()) {
            return getConditionListTooltip(utils, conditions);
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getFunctionListTooltip(IServerUtils utils, List<LootItemFunction> functions) {
        return TooltipBuilder.array((b) -> {
            for (LootItemFunction function : functions) {
                b.add(utils.getFunctionTooltip(utils, function));
            }
        });
    }

    @NotNull
    public static TooltipBuilder getFunctionsTooltip(IServerUtils utils, List<LootItemFunction> functions) {
        if (!functions.isEmpty()) {
            return TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly("ali.util.advanced_loot_info.delimiter.functions"))
                            .add(getFunctionListTooltip(utils, functions))
                    );
        }

        return TooltipBuilder.empty();
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
    public static TooltipBuilder getPropertyMatcherTooltip(IServerUtils ignoredUtils, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        String name = propertyMatcher.name();

        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher(String value)) {
            return TooltipBuilder.keyValue(name, value);
        }
        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher(Optional<String> minValue, Optional<String> maxValue)) {
            if (minValue.isPresent()) {
                if (maxValue.isPresent()) {
                    return TooltipBuilder.value(name, minValue.get(), maxValue.get()).key("ali.property.value.ranged_property_both");
                } else {
                    return TooltipBuilder.value(name, minValue.get()).key("ali.property.value.ranged_property_gte");
                }
            } else {
                if (maxValue.isPresent()) {
                    return TooltipBuilder.value(name, maxValue.get()).key("ali.property.value.ranged_property_lte");
                } else {
                    return TooltipBuilder.value(name).key("ali.property.value.ranged_property_any");
                }
            }
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getStatMatcherTooltip(IServerUtils utils, PlayerPredicate.StatMatcher<?> stat) {
        String key = TooltipBuilder.translate(((TranslatableContents) stat.type().getDisplayName().getContents()).getKey());
        Holder<?> value = stat.value();

        return TooltipBuilder.array((b) -> {
            if (value.value() instanceof Item item) {
                b.add(utils.getValueTooltip(utils, item)
                        .add(TooltipBuilder.keyValue(key, toString(stat.range())).build("aci.util.null"))
                        .build("ali.property.value.item"));
            } else if (value.value() instanceof Block block) {
                b.add(utils.getValueTooltip(utils, block)
                        .add(TooltipBuilder.keyValue(key, toString(stat.range())).build("aci.util.null"))
                        .build("ali.property.value.block"));
            } else if (value.value() instanceof EntityType<?> entityType) {
                b.add(utils.getValueTooltip(utils, entityType)
                        .add(TooltipBuilder.keyValue(key, toString(stat.range())).build("aci.util.null"))
                        .build("ali.property.value.entity_type"));
            } else if (value.value() instanceof Identifier identifier) {
                b.add(utils.getValueTooltip(utils, identifier)
                        .add(TooltipBuilder.keyValue(TooltipBuilder.translate(getTranslationKey(identifier)), toString(stat.range())).build("aci.util.null"))
                        .build("ali.property.value.id"));
            }
        });
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
    public static TooltipNode getDataComponentExactPredicateTooltip(IServerUtils utils, DataComponentExactPredicate dataComponentMatchers) {
        return utils.getValueTooltip(utils, dataComponentMatchers.expectedComponents).build("ali.property.branch.expected_components");
    }

    @NotNull
    public static <T> TooltipBuilder getFilterableTooltip(IServerUtils utils, String value, Collection<Filterable<T>> data) {
        return TooltipBuilder.array((b) -> {
            for (Filterable<T> d : data) {
                b.add(TooltipBuilder.array((c) -> c
                        .add(utils.getValueTooltip(utils, d.raw()).build("ali.property.value.raw"))
                        .add(utils.getValueTooltip(utils, d.filtered()).build("ali.property.value.filtered"))
                ).key(value));
            }
        });
    }

    @NotNull
    public static <T> TooltipBuilder getCollectionTooltip(IServerUtils utils, Collection<T> values, BiFunction<IServerUtils, T, TooltipBuilder> mapper) {
        if (!values.isEmpty()) {
            return TooltipBuilder.array((b) -> values.forEach((value) -> b.add(mapper.apply(utils, value))));
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getCollectionTooltip(IServerUtils utils, String value, @Nullable Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return TooltipBuilder.empty();
        }

        return TooltipBuilder.array((b) -> {
            for (Object o : collection) {
                b.add(utils.getValueTooltip(utils, o).build(value));
            }
        });
    }

    @NotNull
    public static <K, V> TooltipBuilder getMapTooltip(IServerUtils utils, Map<K, V> values, BiFunction<IServerUtils, Map.Entry<K, V>, TooltipBuilder> mapper) {
        if (!values.isEmpty()) {
            return TooltipBuilder.array((b) -> values.entrySet().forEach((e) -> b.add(mapper.apply(utils, e))));
        }

        return TooltipBuilder.empty();
    }

//    // MAP ENTRY

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
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.limit"));
    }

    @NotNull
    public static TooltipBuilder getMobEffectPredicateEntryTooltip(IServerUtils utils, Map.Entry<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue().amplifier()).build("ali.property.value.amplifier"))
                .add(utils.getValueTooltip(utils, entry.getValue().duration()).build("ali.property.value.duration"))
                .add(utils.getValueTooltip(utils, entry.getValue().ambient()).build("ali.property.value.is_ambient"))
                .add(utils.getValueTooltip(utils, entry.getValue().visible()).build("ali.property.value.is_visible"));
    }

    @NotNull
    public static TooltipBuilder getEnchantmentLevelsEntryTooltip(IServerUtils utils, Map.Entry<Holder<Enchantment>, NumberProvider> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.levels"));
    }

    @NotNull
    public static TooltipBuilder getDataComponentEntryTooltip(IServerUtils utils, Map.Entry<DataComponentType<?>, Boolean> entry) {
        return getDataComponentTypeTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.value"));
    }

    @NotNull
    public static TooltipBuilder getAdvancementEntryTooltip(IServerUtils utils, Map.Entry<Identifier, PlayerPredicate.AdvancementPredicate> entry) {
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
    public static TooltipBuilder getDataComponentPatchEntryTooltip(IServerUtils utils, Map.Entry<DataComponentType<?>, Optional<?>> entry) {
        TooltipBuilder builder = getDataComponentTypeTooltip(utils, entry.getKey());

        entry.getValue().ifPresent((v) -> builder.add(utils.getDataComponentTypeTooltip(utils, entry.getKey(), v)));

        if (entry.getValue().isEmpty()) {
            builder.add(TooltipBuilder.keyOnly("aci.util.removed"));
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
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.branch.predicate"));
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
    private static String getTranslationKey(Identifier location) {
        return "stat." + location.toString().replace(':', '.');
    }
}
