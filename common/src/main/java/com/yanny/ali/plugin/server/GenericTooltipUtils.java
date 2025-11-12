package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.*;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.ToggleTooltips;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.*;

public class GenericTooltipUtils {
    @NotNull
    public static ITooltipNode getMissingFunction(IServerUtils utils, LootItemFunction function) {
        return getFunctionTypeTooltip(utils, function.getType()).build("ali.util.advanced_loot_info.missing");

//        Field[] fields = function.getClass().getDeclaredFields();
//        List<Field> names = Arrays.stream(fields).filter((f) -> !Modifier.isStatic(f.getModifiers())).toList();
//
//        //FIXME !!!!!!!!!!!!!!!
//        //TODO !!!!!!!!!!
//
//        names.forEach((f) -> {
//            f.setAccessible(true);
//
//            try {
////                tooltip.add(BranchTooltipNode.branch(f.getName()).add(utils.getValueTooltip(utils, f.get(function))));
//            } catch (IllegalAccessException e) {
//                throw new RuntimeException(e);
//            }
//        });
    }

    @NotNull
    public static ITooltipNode getMissingCondition(IServerUtils utils, LootItemCondition condition) {
        return getConditionTypeTooltip(utils, condition.getType()).build("ali.util.advanced_loot_info.missing");

//        Field[] fields = condition.getClass().getDeclaredFields();
//        List<Field> names = Arrays.stream(fields)
//                .filter((f) -> !Modifier.isStatic(f.getModifiers()))
//                .toList();
    }

    @NotNull
    public static ITooltipNode getConditionListTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        ArrayTooltipNode.Builder array = ArrayTooltipNode.array();

        for (LootItemCondition condition : conditions) {
            array.add(utils.getConditionTooltip(utils, condition));
        }

        return array.build();
    }

    @NotNull
    public static ITooltipNode getConditionsTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        if (!conditions.isEmpty()) {
            return ArrayTooltipNode.array()
                    .add(LiteralTooltipNode.translatable("ali.util.advanced_loot_info.delimiter.conditions"))
                    .add(getConditionListTooltip(utils, conditions))
                    .build();
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static IKeyTooltipNode getSubConditionsTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        if (!conditions.isEmpty()) {
            return BranchTooltipNode.branch().add(getConditionListTooltip(utils, conditions));
        }

        return EmptyTooltipNode.empty();
    }

    @NotNull
    public static ITooltipNode getFunctionListTooltip(IServerUtils utils, List<LootItemFunction> functions) {
        ArrayTooltipNode.Builder array = ArrayTooltipNode.array();

        for (LootItemFunction function : functions) {
            array.add(utils.getFunctionTooltip(utils, function));
        }

        return array.build();
    }

    @NotNull
    public static ITooltipNode getFunctionsTooltip(IServerUtils utils, List<LootItemFunction> functions) {
        if (!functions.isEmpty()) {
            return ArrayTooltipNode.array()
                    .add(LiteralTooltipNode.translatable("ali.util.advanced_loot_info.delimiter.functions"))
                    .add(getFunctionListTooltip(utils, functions))
                    .build();
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getPropertyMatcherTooltip(IServerUtils ignoredUtils, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        String name = propertyMatcher.name();

        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher(String value)) {
            return ValueTooltipNode.keyValue(name, value).build("ali.property.value.null");
        }
        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher(Optional<String> minValue, Optional<String> maxValue)) {
            if (minValue.isPresent()) {
                if (maxValue.isPresent()) {
                    return ValueTooltipNode.value(name, minValue.get(), maxValue.get()).build("ali.property.value.ranged_property_both");
                } else {
                    return ValueTooltipNode.value(name, minValue.get()).build("ali.property.value.ranged_property_gte");
                }
            } else {
                if (maxValue.isPresent()) {
                    return ValueTooltipNode.value(name, maxValue.get()).build("ali.property.value.ranged_property_lte");
                } else {
                    return ValueTooltipNode.value(name).build("ali.property.value.ranged_property_any");
                }
            }
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getStatMatcherTooltip(IServerUtils utils, PlayerPredicate.StatMatcher<?> stat) {
        String key = ValueTooltipNode.translate(((TranslatableContents) stat.type().getDisplayName().getContents()).getKey());
        Holder<?> value = stat.value();
        ITooltipNode tooltip;

        if (value.value() instanceof Item item) {
            tooltip = utils.getValueTooltip(utils, item)
                    .add(ValueTooltipNode.keyValue(key, toString(stat.range())).build("ali.property.value.null"))
                    .build("ali.property.value.item");
        } else if (value.value() instanceof Block block) {
            tooltip = utils.getValueTooltip(utils, block)
                    .add(ValueTooltipNode.keyValue(key, toString(stat.range())).build("ali.property.value.null"))
                    .build("ali.property.value.block");
        } else if (value.value() instanceof EntityType<?> entityType) {
            tooltip = utils.getValueTooltip(utils, entityType)
                    .add(ValueTooltipNode.keyValue(key, toString(stat.range())).build("ali.property.value.null"))
                    .build("ali.property.value.entity_type");
        } else if (value.value() instanceof ResourceLocation resourceLocation) {
            tooltip = utils.getValueTooltip(utils, resourceLocation)
                    .add(ValueTooltipNode.keyValue(ValueTooltipNode.translate(getTranslationKey(resourceLocation)), toString(stat.range())).build("ali.property.value.null"))
                    .build("ali.property.value.id");
        } else {
            tooltip = EmptyTooltipNode.EMPTY;
        }

        return tooltip;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <T> IKeyTooltipNode getStandaloneTooltip(IServerUtils utils, String value, Optional<ListOperation.StandAlone<T>> standalone) {
        if (standalone.isPresent()) {
            IKeyTooltipNode tooltip = BranchTooltipNode.branch();
            ListOperation.StandAlone<T> s = standalone.get();

            tooltip.add(getCollectionTooltip(utils, value, s.value()).build("ali.property.branch.values"));
            tooltip.add(utils.getValueTooltip(utils, s.operation()).build("ali.property.value.list_operation"));

            return tooltip;
        }

        return EmptyTooltipNode.empty();
    }

    @NotNull
    public static <T> IKeyTooltipNode getFilterableTooltip(IServerUtils utils, String value, Collection<Filterable<T>> data) {
        IKeyTooltipNode tooltips = BranchTooltipNode.branch();

        for (Filterable<T> d : data) {
            tooltips.add(BranchTooltipNode.branch()
                    .add(utils.getValueTooltip(utils, d.raw()).build("ali.property.value.raw"))
                    .add(utils.getValueTooltip(utils, d.filtered()).build("ali.property.value.filtered"))
                    .build(value));
        }

        return tooltips;
    }

    @NotNull
    public static <T> IKeyTooltipNode getCollectionTooltip(IServerUtils utils, Collection<T> values, BiFunction<IServerUtils, T, ITooltipNode> mapper) {
        if (!values.isEmpty()) {
            IKeyTooltipNode tooltip = BranchTooltipNode.branch();

            values.forEach((value) -> tooltip.add(mapper.apply(utils, value)));
            return tooltip;
        }

        return EmptyTooltipNode.empty();
    }

    @NotNull
    public static IKeyTooltipNode getCollectionTooltip(IServerUtils utils, String value, @Nullable Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return EmptyTooltipNode.empty();
        }

        BranchTooltipNode.Builder tooltip = BranchTooltipNode.branch();

        for (Object o : collection) {
            tooltip.add(utils.getValueTooltip(utils, o).build(value));
        }

        return tooltip;
    }

    @NotNull
    public static <K, V> IKeyTooltipNode getMapTooltip(IServerUtils utils, Map<K, V> values, BiFunction<IServerUtils, Map.Entry<K, V>, ITooltipNode> mapper) {
        if (!values.isEmpty()) {
            BranchTooltipNode.Builder tooltip = BranchTooltipNode.branch();

            values.entrySet().forEach((e) -> tooltip.add(mapper.apply(utils, e)));
            return tooltip;
        }

        return EmptyTooltipNode.empty();
    }

    // MAP ENTRY

    @NotNull
    public static ITooltipNode getRecipeEntryTooltip(IServerUtils ignoredUtils, Map.Entry<ResourceLocation, Boolean> entry) {
        return ValueTooltipNode.keyValue(entry.getKey(), entry.getValue()).build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getCriterionEntryTooltip(IServerUtils ignoredUtils, Map.Entry<String, Boolean> entry) {
        return ValueTooltipNode.keyValue(entry.getKey(), entry.getValue()).build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getStringEntryTooltip(IServerUtils ignoredUtils, Map.Entry<String, String> entry) {
        return ValueTooltipNode.keyValue(entry.getKey(), entry.getValue()).build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getIntRangeEntryTooltip(IServerUtils utils, Map.Entry<String, IntRange> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.limit"))
                .build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getMobEffectPredicateEntryTooltip(IServerUtils utils, Map.Entry<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue().amplifier()).build("ali.property.value.amplifier"))
                .add(utils.getValueTooltip(utils, entry.getValue().duration()).build("ali.property.value.duration"))
                .add(utils.getValueTooltip(utils, entry.getValue().ambient()).build("ali.property.value.is_ambient"))
                .add(utils.getValueTooltip(utils, entry.getValue().visible()).build("ali.property.value.is_visible"))
                .build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getEnchantmentLevelsEntryTooltip(IServerUtils utils, Map.Entry<Holder<Enchantment>, NumberProvider> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.levels"))
                .build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getAdvancementEntryTooltip(IServerUtils utils, Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(getAdvancementPredicateTooltip(utils, entry.getValue()))
                .build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getMapDecorationEntryTooltip(IServerUtils utils, Map.Entry<String, MapDecorations.Entry> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.null"))
                .build("ali.property.value.decoration");
    }

    @NotNull
    public static ITooltipNode getBlockPropertyEntryTooltip(IServerUtils utils, Map.Entry<Holder<Block>, Property<?>> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.property"))
                .build("ali.property.value.block");
    }

    @NotNull
    public static ITooltipNode getPropertiesEntryTooltip(IServerUtils utils, Map.Entry<String, Collection<com.mojang.authlib.properties.Property>> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(GenericTooltipUtils.getCollectionTooltip(utils, "ali.property.branch.property", entry.getValue()).build("ali.property.branch.properties"))
                .build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getEnchantmentLevelEntryTooltip(IServerUtils utils, Map.Entry<Holder<Enchantment>, Integer> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.level"))
                .build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getToggleEntryTooltip(IServerUtils utils, Map.Entry<ToggleTooltips.ComponentToggle<?>, Boolean> entry) {
        return getDataComponentTypeTooltip(utils, entry.getKey().type())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.value"))
                .build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getDataComponentPatchEntryTooltip(IServerUtils utils, Map.Entry<DataComponentType<?>, Optional<?>> entry) {
        IKeyTooltipNode tooltip = getDataComponentTypeTooltip(utils, entry.getKey());

        entry.getValue().ifPresent((v) -> tooltip.add(utils.getDataComponentTypeTooltip(utils, entry.getKey(), v)));

        if (entry.getValue().isEmpty()) {
            tooltip.add(LiteralTooltipNode.translatable("ali.util.advanced_loot_info.removed"));
        }

        return tooltip.build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getItemSubPredicateEntryTooltip(IServerUtils utils, Map.Entry<ItemSubPredicate.Type<?>, ItemSubPredicate> entry) {
        return utils.getItemSubPredicateTooltip(utils, entry.getValue());
    }

    @NotNull
    public static ITooltipNode getSlotRangePredicateEntryTooltip(IServerUtils utils, Map.Entry<SlotRange, ItemPredicate> entry) {
//        return utils.getValueTooltip(utils, entry.getKey().slots())
        return ValueTooltipNode.keyValue(entry.getKey().toString(), entry.getKey().slots().toString())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.branch.predicate"))
                .build("ali.property.value.null");
    }

    @NotNull
    public static <V, T extends Registry<V>> IKeyTooltipNode getRegistryTooltip(IServerUtils utils, ResourceKey<T> registry, V value) {
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

        return EmptyTooltipNode.empty();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <A, B extends Predicate<A>> IKeyTooltipNode getCollectionPredicateTooltip(IServerUtils utils, String value, Optional<CollectionPredicate<A, B>> optional) {
        if (optional.isPresent()) {
            CollectionPredicate<A, B> predicate = optional.get();

            return BranchTooltipNode.branch()
                    .add(getCollectionContentsPredicateTooltip(utils, value, predicate.contains()).build("ali.property.branch.contains"))
                    .add(getCollectionCountsPredicateTooltip(utils, value, predicate.counts()).build("ali.property.branch.counts"))
                    .add(utils.getValueTooltip(utils, predicate.size()).build("ali.property.value.size"));
        }

        return EmptyTooltipNode.empty();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <A, B extends Predicate<A>> IKeyTooltipNode getCollectionContentsPredicateTooltip(IServerUtils utils, String value, Optional<CollectionContentsPredicate<A, B>> predicate) {
        return predicate.map((p) -> getCollectionTooltip(utils, value, p.unpack())).orElse(EmptyTooltipNode.empty());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <A, B extends Predicate<A>> IKeyTooltipNode getCollectionCountsPredicateTooltip(IServerUtils utils, String value, Optional<CollectionCountsPredicate<A, B>> predicate) {
        return predicate.map((p) -> getCollectionTooltip(utils, value, p.unpack())).orElse(EmptyTooltipNode.empty());
    }

    @NotNull
    public static ITooltipNode getAdvancementPredicateTooltip(IServerUtils utils, PlayerPredicate.AdvancementPredicate predicate) {
        if (predicate instanceof PlayerPredicate.AdvancementDonePredicate(boolean state)) {
            return utils.getValueTooltip(utils, state).build("ali.property.value.done");
        } else if (predicate instanceof PlayerPredicate.AdvancementCriterionsPredicate(
                Object2BooleanMap<String> criterions)) {
            return getMapTooltip(utils, criterions, GenericTooltipUtils::getCriterionEntryTooltip).build("ali.property.branch.criterions");
        }

        return EmptyTooltipNode.EMPTY;
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
