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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
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
        IKeyTooltipNode tooltip = getFunctionTypeTooltip(utils, function.getType()).key("ali.util.advanced_loot_info.missing");

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

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMissingCondition(IServerUtils utils, LootItemCondition condition) {
        ITooltipNode tooltip = getConditionTypeTooltip(utils, condition.getType()).key("ali.util.advanced_loot_info.missing");

//        Field[] fields = condition.getClass().getDeclaredFields();
//        List<Field> names = Arrays.stream(fields)
//                .filter((f) -> !Modifier.isStatic(f.getModifiers()))
//                .toList();

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getConditionListTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        ArrayTooltipNode array = ArrayTooltipNode.array();

        for (LootItemCondition condition : conditions) {
            array.add(utils.getConditionTooltip(utils, condition));
        }

        return array;
    }

    @NotNull
    public static ITooltipNode getConditionsTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        if (!conditions.isEmpty()) {
            return ArrayTooltipNode.array()
                    .add(LiteralTooltipNode.translatable("ali.util.advanced_loot_info.delimiter.conditions"))
                    .add(getConditionListTooltip(utils, conditions));
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static IKeyTooltipNode getSubConditionsTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        if (!conditions.isEmpty()) {
            return BranchTooltipNode.branch().add(getConditionListTooltip(utils, conditions));
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getFunctionListTooltip(IServerUtils utils, List<LootItemFunction> functions) {
        ArrayTooltipNode array = ArrayTooltipNode.array();

        for (LootItemFunction function : functions) {
            array.add(utils.getFunctionTooltip(utils, function));
        }

        return array;
    }

    @NotNull
    public static ITooltipNode getFunctionsTooltip(IServerUtils utils, List<LootItemFunction> functions) {
        if (!functions.isEmpty()) {
            return ArrayTooltipNode.array()
                    .add(LiteralTooltipNode.translatable("ali.util.advanced_loot_info.delimiter.functions"))
                    .add(getFunctionListTooltip(utils, functions));
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static IKeyTooltipNode getPropertyMatcherTooltip(IServerUtils ignoredUtils, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        String name = propertyMatcher.name();

        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher(String value)) {
            return ValueTooltipNode.keyValue(name, value).key("ali.property.value.null");
        }
        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher(Optional<String> minValue, Optional<String> maxValue)) {
            if (minValue.isPresent()) {
                if (maxValue.isPresent()) {
                    return ValueTooltipNode.value(name, minValue.get(), maxValue.get()).key("ali.property.value.ranged_property_both");
                } else {
                    return ValueTooltipNode.value(name, minValue.get()).key("ali.property.value.ranged_property_gte");
                }
            } else {
                if (maxValue.isPresent()) {
                    return ValueTooltipNode.value(name, maxValue.get()).key("ali.property.value.ranged_property_lte");
                } else {
                    return ValueTooltipNode.value(name).key("ali.property.value.ranged_property_any");
                }
            }
        }

        return EmptyTooltipNode.EMPTY;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getGameTypePredicateTooltip(IServerUtils utils, String key, GameTypePredicate gameType) {
        return getCollectionTooltip(utils, key, "ali.property.value.null", gameType.types(), GenericTooltipUtils::getEnumTooltip);
    }

    @NotNull
    public static IKeyTooltipNode getStatMatcherTooltip(IServerUtils utils, PlayerPredicate.StatMatcher<?> stat) {
        String key = ValueTooltipNode.translate(((TranslatableContents) stat.type().getDisplayName().getContents()).getKey());
        Holder<?> value = stat.value();
        IKeyTooltipNode tooltip;

        if (value.value() instanceof Item item) {
            tooltip = utils.getValueTooltip(utils, item).key("ali.property.value.item");
            tooltip.add(ValueTooltipNode.keyValue(key, toString(stat.range())).key("ali.property.value.null"));
        } else if (value.value() instanceof Block block) {
            tooltip = utils.getValueTooltip(utils, block).key("ali.property.value.block");
            tooltip.add(ValueTooltipNode.keyValue(key, toString(stat.range())).key("ali.property.value.null"));
        } else if (value.value() instanceof EntityType<?> entityType) {
            tooltip = utils.getValueTooltip(utils, entityType).key("ali.property.value.entity_type");
            tooltip.add(ValueTooltipNode.keyValue(key, toString(stat.range())).key("ali.property.value.null"));
        } else if (value.value() instanceof ResourceLocation resourceLocation) {
            tooltip = utils.getValueTooltip(utils, resourceLocation).key("ali.property.value.id");
            tooltip.add(ValueTooltipNode.keyValue(ValueTooltipNode.translate(getTranslationKey(resourceLocation)), toString(stat.range())).key("ali.property.value.null"));
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

            tooltip.add(getCollectionTooltip(utils, value, s.value()).key("ali.property.branch.values"));
            tooltip.add(utils.getValueTooltip(utils, s.operation()).key("ali.property.value.list_operation"));

            return tooltip;
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static <T> IKeyTooltipNode getFilterableTooltip(IServerUtils utils, String value, Collection<Filterable<T>> data) {
        IKeyTooltipNode tooltips = BranchTooltipNode.branch();

        for (Filterable<T> d : data) {
            tooltips.add(BranchTooltipNode.branch(value)
                    .add(utils.getValueTooltip(utils, d.raw()).key("ali.property.value.raw"))
                    .add(utils.getValueTooltip(utils, d.filtered()).key("ali.property.value.filtered")));
        }

        return tooltips;
    }

    @NotNull
    public static ITooltipNode getLevelBasedValueTooltip(IServerUtils utils, String key, LevelBasedValue levelBasedValue) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        switch (levelBasedValue) {
            case LevelBasedValue.Constant(float value) ->
                    tooltip.add(getFloatTooltip(utils, "ali.property.value.constant", value));
            case LevelBasedValue.Clamped(LevelBasedValue value, float min, float max) -> {
                ITooltipNode t = new TooltipNode(translatable("ali.property.branch.clamped"));

                t.add(getLevelBasedValueTooltip(utils, "ali.property.branch.value", value));
                t.add(getFloatTooltip(utils, "ali.property.value.min", min));
                t.add(getFloatTooltip(utils, "ali.property.value.max", max));
                tooltip.add(t);
            }
            case LevelBasedValue.Fraction(LevelBasedValue numerator, LevelBasedValue denominator) -> {
                ITooltipNode t = new TooltipNode(translatable("ali.property.branch.fraction"));

                t.add(getLevelBasedValueTooltip(utils, "ali.property.branch.numerator", numerator));
                t.add(getLevelBasedValueTooltip(utils, "ali.property.branch.denominator", denominator));
                tooltip.add(t);
            }
            case LevelBasedValue.Linear(float base, float perLevelAboveFirst) -> {
                ITooltipNode t = new TooltipNode(translatable("ali.property.branch.linear"));

                t.add(getFloatTooltip(utils, "ali.property.value.base", base));
                t.add(getFloatTooltip(utils, "ali.property.value.per_level", perLevelAboveFirst));
                tooltip.add(t);
            }
            case LevelBasedValue.LevelsSquared(float added) -> {
                ITooltipNode t = new TooltipNode(translatable("ali.property.branch.level_squared"));

                t.add(getFloatTooltip(utils, "ali.property.value.added", added));
                tooltip.add(t);
            }
            case LevelBasedValue.Lookup(List<Float> values, LevelBasedValue fallback) -> {
                ITooltipNode t = new TooltipNode(translatable("ali.property.branch.lookup"));

                t.add(getStringTooltip(utils, "ali.property.value.values", values.toString()));
                t.add(getLevelBasedValueTooltip(utils, "ali.property.branch.fallback", fallback));
                tooltip.add(t);
            }
            default -> {
            }
        }

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getLocationWrapperTooltip(IServerUtils utils, String key, EntityPredicate.LocationWrapper locationWrapper) {
        if (locationWrapper.located().isPresent() || locationWrapper.affectsMovement().isPresent() || locationWrapper.steppingOn().isPresent()) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.located", locationWrapper.located(), GenericTooltipUtils::getLocationPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.stepping_on_location", locationWrapper.steppingOn(), GenericTooltipUtils::getLocationPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.affects_movement", locationWrapper.affectsMovement(), GenericTooltipUtils::getLocationPredicateTooltip));

            return tooltip;
        }

        return new TooltipNode();
    }

    @NotNull
    public static ITooltipNode getMovementPredicateTooltip(IServerUtils utils, String key, MovementPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.x", predicate.x()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.y", predicate.y()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.z", predicate.z()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.speed", predicate.speed()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.horizontal_speed", predicate.horizontalSpeed()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.vertical_speed", predicate.verticalSpeed()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.fall_distance", predicate.fallDistance()));

        return tooltip;
    }

    @NotNull
    public static <T> IKeyTooltipNode getCollectionTooltip(IServerUtils utils, Collection<T> values, BiFunction<IServerUtils, T, ITooltipNode> mapper) {
        if (!values.isEmpty()) {
            IKeyTooltipNode tooltip = BranchTooltipNode.branch();

            values.forEach((value) -> tooltip.add(mapper.apply(utils, value)));
            return tooltip;
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static IKeyTooltipNode getCollectionTooltip(IServerUtils utils, String value, @Nullable Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return EmptyTooltipNode.EMPTY;
        }

        BranchTooltipNode tooltip = BranchTooltipNode.branch();

        for (Object o : collection) {
            tooltip.add(utils.getValueTooltip(utils, o).key(value));
        }

        return tooltip;
    }

    @NotNull
    public static <K, V> IKeyTooltipNode getMapTooltip(IServerUtils utils, Map<K, V> values, BiFunction<IServerUtils, Map.Entry<K, V>, ITooltipNode> mapper) {
        if (!values.isEmpty()) {
            BranchTooltipNode tooltip = BranchTooltipNode.branch();

            values.entrySet().forEach((e) -> tooltip.add(mapper.apply(utils, e)));
            return tooltip;
        }

        return EmptyTooltipNode.EMPTY;
    }

    // MAP ENTRY

    @NotNull
    public static IKeyTooltipNode getRecipeEntryTooltip(IServerUtils ignoredUtils, Map.Entry<ResourceLocation, Boolean> entry) {
        return ValueTooltipNode.keyValue(entry.getKey(), entry.getValue()).key("ali.property.value.null");
    }

    @NotNull
    public static IKeyTooltipNode getCriterionEntryTooltip(IServerUtils ignoredUtils, Map.Entry<String, Boolean> entry) {
        return ValueTooltipNode.keyValue(entry.getKey(), entry.getValue()).key("ali.property.value.null");
    }

    @NotNull
    public static IKeyTooltipNode getStringEntryTooltip(IServerUtils ignoredUtils, Map.Entry<String, String> entry) {
        return ValueTooltipNode.keyValue(entry.getKey(), entry.getValue()).key("ali.property.value.null");
    }

    @NotNull
    public static IKeyTooltipNode getIntRangeEntryTooltip(IServerUtils utils, Map.Entry<String, IntRange> entry) {
        return utils.getValueTooltip(utils, entry.getKey()).key("ali.property.value.null")
                .add(utils.getValueTooltip(utils, entry.getValue()).key("ali.property.value.limit"));
    }

    @NotNull
    public static IKeyTooltipNode getMobEffectPredicateEntryTooltip(IServerUtils utils, Map.Entry<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey()).key("ali.property.value.null")
                .add(utils.getValueTooltip(utils, entry.getValue().amplifier()).key("ali.property.value.amplifier"))
                .add(utils.getValueTooltip(utils, entry.getValue().duration()).key("ali.property.value.duration"))
                .add(utils.getValueTooltip(utils, entry.getValue().ambient()).key("ali.property.value.is_ambient"))
                .add(utils.getValueTooltip(utils, entry.getValue().visible()).key("ali.property.value.is_visible"));
    }

    @NotNull
    public static IKeyTooltipNode getEnchantmentLevelsEntryTooltip(IServerUtils utils, Map.Entry<Holder<Enchantment>, NumberProvider> entry) {
        return utils.getValueTooltip(utils, entry.getKey()).key("ali.property.value.null")
                .add(utils.getValueTooltip(utils, entry.getValue()).key("ali.property.value.levels"));
    }

    @NotNull
    public static IKeyTooltipNode getAdvancementEntryTooltip(IServerUtils utils, Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey()).key("ali.property.value.null")
                .add(getAdvancementPredicateTooltip(utils, entry.getValue()));
    }

    @NotNull
    public static IKeyTooltipNode getMapDecorationEntryTooltip(IServerUtils utils, Map.Entry<String, MapDecorations.Entry> entry) {
        return utils.getValueTooltip(utils, entry.getKey()).key("ali.property.value.decoration")
                .add(utils.getValueTooltip(utils, entry.getValue()).key("ali.property.value.null"));
    }

    @NotNull
    public static IKeyTooltipNode getBlockPropertyEntryTooltip(IServerUtils utils, Map.Entry<Holder<Block>, Property<?>> entry) {
        return utils.getValueTooltip(utils, entry.getKey()).key("ali.property.value.block")
                .add(utils.getValueTooltip(utils, entry.getValue()).key("ali.property.value.property"));
    }

    @NotNull
    public static IKeyTooltipNode getPropertiesEntryTooltip(IServerUtils utils, Map.Entry<String, Collection<com.mojang.authlib.properties.Property>> entry) {
        return utils.getValueTooltip(utils, entry.getKey()).key("ali.property.value.null")
                .add(GenericTooltipUtils.getCollectionTooltip(utils, "ali.property.branch.property", entry.getValue()).key("ali.property.branch.properties"));
    }

    @NotNull
    public static IKeyTooltipNode getEnchantmentLevelEntryTooltip(IServerUtils utils, Map.Entry<Holder<Enchantment>, Integer> entry) {
        return utils.getValueTooltip(utils, entry.getKey()).key("ali.property.value.null")
                .add(utils.getValueTooltip(utils, entry.getValue()).key("ali.property.value.level"));
    }

    @NotNull
    public static IKeyTooltipNode getToggleEntryTooltip(IServerUtils utils, Map.Entry<ToggleTooltips.ComponentToggle<?>, Boolean> entry) {
        return getDataComponentTypeTooltip(utils, entry.getKey().type()).key("ali.property.value.null")
                .add(utils.getValueTooltip(utils, entry.getValue()).key("ali.property.value.value"));
    }

    @NotNull
    public static IKeyTooltipNode getDataComponentPatchEntryTooltip(IServerUtils utils, Map.Entry<DataComponentType<?>, Optional<?>> entry) {
        IKeyTooltipNode tooltip = getDataComponentTypeTooltip(utils, entry.getKey()).key("ali.property.value.null");

        entry.getValue().ifPresent((v) -> tooltip.add(utils.getDataComponentTypeTooltip(utils, entry.getKey(), v)));

        if (entry.getValue().isEmpty()) {
            tooltip.add(LiteralTooltipNode.translatable("ali.util.advanced_loot_info.removed"));
        }

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getItemSubPredicateEntryTooltip(IServerUtils utils, Map.Entry<ItemSubPredicate.Type<?>, ItemSubPredicate> entry) {
        return utils.getItemSubPredicateTooltip(utils, entry.getValue());
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

        return EmptyTooltipNode.EMPTY;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <A, B extends Predicate<A>> IKeyTooltipNode getCollectionPredicateTooltip(IServerUtils utils, String value, Optional<CollectionPredicate<A, B>> optional) {
        if (optional.isPresent()) {
            CollectionPredicate<A, B> predicate = optional.get();

            return BranchTooltipNode.branch()
                    .add(getCollectionContentsPredicateTooltip(utils, value, predicate.contains()).key("ali.property.branch.contains"))
                    .add(getCollectionCountsPredicateTooltip(utils, value, predicate.counts()).key("ali.property.branch.counts"))
                    .add(utils.getValueTooltip(utils, predicate.size()).key("ali.property.value.size"));
        }

        return EmptyTooltipNode.EMPTY;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <A, B extends Predicate<A>> IKeyTooltipNode getCollectionContentsPredicateTooltip(IServerUtils utils, String value, Optional<CollectionContentsPredicate<A, B>> predicate) {
        return predicate.map((p) -> getCollectionTooltip(utils, value, p.unpack())).orElse(EmptyTooltipNode.EMPTY);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <A, B extends Predicate<A>> IKeyTooltipNode getCollectionCountsPredicateTooltip(IServerUtils utils, String value, Optional<CollectionCountsPredicate<A, B>> predicate) {
        return predicate.map((p) -> getCollectionTooltip(utils, value, p.unpack())).orElse(EmptyTooltipNode.EMPTY);
    }

    @NotNull
    public static ITooltipNode getAdvancementPredicateTooltip(IServerUtils utils, PlayerPredicate.AdvancementPredicate predicate) {
        if (predicate instanceof PlayerPredicate.AdvancementDonePredicate(boolean state)) {
            return utils.getValueTooltip(utils, state).key("ali.property.value.done");
        } else if (predicate instanceof PlayerPredicate.AdvancementCriterionsPredicate(
                Object2BooleanMap<String> criterions)) {
            return getMapTooltip(utils, criterions, GenericTooltipUtils::getCriterionEntryTooltip).key("ali.property.branch.criterions");
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
