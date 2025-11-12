package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.*;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.getConditionTypeTooltip;
import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.getFunctionTypeTooltip;

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
        if (propertyMatcher instanceof StatePropertiesPredicate.ExactPropertyMatcher matcher) {
            return ValueTooltipNode.value(matcher.name, matcher.value).build("ali.util.advanced_loot_info.key_value");
        }
        if (propertyMatcher instanceof StatePropertiesPredicate.RangedPropertyMatcher matcher) {
            String min = matcher.minValue;
            String max = matcher.maxValue;

            if (min != null) {
                if (max != null) {
                    return ValueTooltipNode.value(matcher.name, min, max).build("ali.property.value.ranged_property_both");
                } else {
                    return ValueTooltipNode.value(matcher.name, min).build("ali.property.value.ranged_property_gte");
                }
            } else {
                if (max != null) {
                    return ValueTooltipNode.value(matcher.name, max).build("ali.property.value.ranged_property_lte");
                } else {
                    return ValueTooltipNode.value(matcher.name).build("ali.property.value.ranged_property_any");
                }
            }
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static IKeyTooltipNode getStatsTooltip(IServerUtils utils, Map<Stat<?>, MinMaxBounds.Ints> statIntsMap) {
        if (!statIntsMap.isEmpty()) {
            IKeyTooltipNode tooltip = BranchTooltipNode.branch();

            statIntsMap.forEach((stat, ints) -> {
                Object value = stat.getValue();

                if (value instanceof Item item) {
                    IKeyTooltipNode itemTooltip = utils.getValueTooltip(utils, item);

                    itemTooltip.add(ValueTooltipNode.keyValue(ValueTooltipNode.translate(stat.getType().getTranslationKey()), toString(ints)).build("ali.property.value.null"));
                    tooltip.add(itemTooltip.build("ali.property.value.item"));
                } else if (value instanceof Block block) {
                    IKeyTooltipNode blockTooltip = utils.getValueTooltip(utils, block);

                    blockTooltip.add(ValueTooltipNode.keyValue(ValueTooltipNode.translate(stat.getType().getTranslationKey()), toString(ints)).build("ali.property.value.null"));
                    tooltip.add(blockTooltip.build("ali.property.value.block"));
                } else if (value instanceof EntityType<?> entityType) {
                    IKeyTooltipNode entityTooltip = utils.getValueTooltip(utils, entityType);

                    entityTooltip.add(ValueTooltipNode.keyValue(ValueTooltipNode.translate(stat.getType().getTranslationKey()), toString(ints)).build("ali.property.value.null"));
                    tooltip.add(entityTooltip.build("ali.property.value.entity_type"));
                } else if (value instanceof ResourceLocation resourceLocation) {
                    IKeyTooltipNode locationTooltip = utils.getValueTooltip(utils, resourceLocation);

                    locationTooltip.add(ValueTooltipNode.keyValue(ValueTooltipNode.translate(getTranslationKey(resourceLocation)), toString(ints)).build("ali.property.value.null"));
                    tooltip.add(locationTooltip.build("ali.property.value.id"));
                }
            });

            return tooltip;
        }

        return EmptyTooltipNode.empty();
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
    public static ITooltipNode getIntRangeEntryTooltip(IServerUtils utils, Map.Entry<String, IntRange> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.limit"))
                .build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getMobEffectPredicateEntryTooltip(IServerUtils utils, Map.Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue().amplifier).build("ali.property.value.amplifier"))
                .add(utils.getValueTooltip(utils, entry.getValue().duration).build("ali.property.value.duration"))
                .add(utils.getValueTooltip(utils, entry.getValue().ambient).build("ali.property.value.is_ambient"))
                .add(utils.getValueTooltip(utils, entry.getValue().visible).build("ali.property.value.is_visible"))
                .build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getEnchantmentLevelsEntryTooltip(IServerUtils utils, Map.Entry<Enchantment, NumberProvider> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.levels"))
                .build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getMobEffectDurationEntryTooltip(IServerUtils utils, Map.Entry<MobEffect, NumberProvider> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.duration"))
                .build("ali.property.value.null");
    }

    @NotNull
    public static ITooltipNode getAdvancementEntryTooltip(IServerUtils utils, Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry) {
        IKeyTooltipNode tooltip = utils.getValueTooltip(utils, entry.getKey());
        IKeyTooltipNode value = utils.getValueTooltip(utils, entry.getValue());

        if (value instanceof ValueTooltipNode.Builder) {
            tooltip.add(value.build("ali.property.value.done"));
        } else {
            tooltip.add(value.build("ali.property.branch.criterions"));
        }

        return tooltip.build("ali.property.value.null");
    }

    @NotNull
    public static String toString(MinMaxBounds.Doubles doubles) {
        Double min = doubles.getMin();
        Double max = doubles.getMax();

        if (min != null) {
            if (max != null) {
                if (!Objects.equals(min, max)) {
                    return String.format("%.1f-%.1f", min, max);
                } else {
                    return String.format("=%.1f", min);
                }
            } else {
                return String.format("≥%.1f", min);
            }
        } else {
            if (max != null) {
                return String.format("≤%.1f", max);
            }

            return "???";
        }
    }

    @NotNull
    public static String toString(MinMaxBounds.Ints ints) {
        Integer min = ints.getMin();
        Integer max = ints.getMax();

        if (min != null) {
            if (max != null) {
                if (!Objects.equals(min, max)) {
                    return String.format("%d-%d", min, max);
                } else {
                    return String.format("=%d", min);
                }
            } else {
                return String.format("≥%d", min);
            }
        } else {
            if (max != null) {
                return String.format("≤%d", max);
            }

            return "???";
        }
    }

    @NotNull
    private static String getTranslationKey(ResourceLocation location) {
        return "stat." + location.toString().replace(':', '.');
    }
}
