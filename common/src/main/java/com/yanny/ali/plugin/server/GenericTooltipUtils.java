package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.*;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.*;

public class GenericTooltipUtils {
    @NotNull
    public static ITooltipNode getMissingEntryTooltip(IServerUtils utils, LootPoolEntryContainer entry) {
        IKeyTooltipNode tooltip = getEntryTypeTooltip(utils, entry.getType());

        TooltipUtils.addObjectFields(utils, tooltip, entry);
        return tooltip.build("ali.util.advanced_loot_info.auto_detected");
    }

    @NotNull
    public static ITooltipNode getMissingFunctionTooltip(IServerUtils utils, LootItemFunction function) {
        IKeyTooltipNode tooltip = getFunctionTypeTooltip(utils, function.getType());

        TooltipUtils.addObjectFields(utils, tooltip, function);
        return tooltip.build("ali.util.advanced_loot_info.auto_detected");
    }

    @NotNull
    public static ITooltipNode getMissingConditionTooltip(IServerUtils utils, LootItemCondition condition) {
        IKeyTooltipNode tooltip = getConditionTypeTooltip(utils, condition.getType());

        TooltipUtils.addObjectFields(utils, tooltip, condition);
        return tooltip.build("ali.util.advanced_loot_info.auto_detected");
    }

    @NotNull
    public static ITooltipNode getMissingItemListingTooltip(IServerUtils utils, VillagerTrades.ItemListing itemListing) {
        IKeyTooltipNode tooltip = ValueTooltipNode.value(itemListing.getClass().getName());

        TooltipUtils.addObjectFields(utils, tooltip, itemListing);
        return tooltip.build("ali.util.advanced_loot_info.auto_detected");
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

        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher matcher) {
            return ValueTooltipNode.keyValue(name, matcher.value()).build("ali.property.value.null");
        }
        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher matcher) {
            Optional<String> min = matcher.minValue();
            Optional<String> max = matcher.maxValue();

            if (min.isPresent()) {
                if (max.isPresent()) {
                    return ValueTooltipNode.value(name, min.get(), max.get()).build("ali.property.value.ranged_property_both");
                } else {
                    return ValueTooltipNode.value(name, min.get()).build("ali.property.value.ranged_property_gte");
                }
            } else {
                if (max.isPresent()) {
                    return ValueTooltipNode.value(name, max.get()).build("ali.property.value.ranged_property_lte");
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
