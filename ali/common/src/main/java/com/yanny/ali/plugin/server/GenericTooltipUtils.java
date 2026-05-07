package com.yanny.ali.plugin.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.*;

public class GenericTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static TooltipNode getMissingEntryTooltip(IServerUtils utils, LootPoolEntryContainer entry) {
        TooltipBuilder tooltip = getEntryTypeTooltip(utils, entry.getType());

        try {
            Gson lootGson = Deserializers.createLootTableSerializer().create();
            JsonElement jsonElement = lootGson.toJsonTree(entry);

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
            Gson lootGson = Deserializers.createFunctionSerializer().create();
            JsonElement jsonElement = lootGson.toJsonTree(function);

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
            Gson lootGson = Deserializers.createConditionSerializer().create();
            JsonElement jsonElement = lootGson.toJsonTree(condition);

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
            tooltip.add(TooltipUtils.getJsonTooltip(utils, ingredient.toJson()));
        } catch (Throwable e) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.warn("Failed to get ingredient info from serialized data for {} in {}", ingredient.getClass().getName(), utils.getCurrentLootTable(), e);
            }

            TooltipUtils.addObjectFields(utils, tooltip, ingredient, Ingredient.class);
        }

        return tooltip.build("aci.util.auto_detected");
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
    public static TooltipBuilder getPropertyMatcherTooltip(IServerUtils ignoredUtils, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        if (propertyMatcher instanceof StatePropertiesPredicate.ExactPropertyMatcher matcher) {
            return TooltipBuilder.keyValue(matcher.name, matcher.value);
        }
        if (propertyMatcher instanceof StatePropertiesPredicate.RangedPropertyMatcher matcher) {
            String min = matcher.minValue;
            String max = matcher.maxValue;

            if (min != null) {
                if (max != null) {
                    return TooltipBuilder.value(matcher.name, min, max).key("ali.property.value.ranged_property_both");
                } else {
                    return TooltipBuilder.value(matcher.name, min).key("ali.property.value.ranged_property_gte");
                }
            } else {
                if (max != null) {
                    return TooltipBuilder.value(matcher.name, max).key("ali.property.value.ranged_property_lte");
                } else {
                    return TooltipBuilder.value(matcher.name).key("ali.property.value.ranged_property_any");
                }
            }
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getStatsTooltip(IServerUtils utils, Map<Stat<?>, MinMaxBounds.Ints> statIntsMap) {
        if (!statIntsMap.isEmpty()) {
            return TooltipBuilder.array((b) -> {
                statIntsMap.forEach((stat, ints) -> {
                    Object value = stat.getValue();

                    if (value instanceof Item item) {
                        TooltipBuilder itemTooltip = utils.getValueTooltip(utils, item);

                        itemTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(stat.getType().getTranslationKey()), toString(ints)).build());
                        b.add(itemTooltip.build("ali.property.value.item"));
                    } else if (value instanceof Block block) {
                        TooltipBuilder blockTooltip = utils.getValueTooltip(utils, block);

                        blockTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(stat.getType().getTranslationKey()), toString(ints)).build());
                        b.add(blockTooltip.build("ali.property.value.block"));
                    } else if (value instanceof EntityType<?> entityType) {
                        TooltipBuilder entityTooltip = utils.getValueTooltip(utils, entityType);

                        entityTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(stat.getType().getTranslationKey()), toString(ints)).build());
                        b.add(entityTooltip.build("ali.property.value.entity_type"));
                    } else if (value instanceof ResourceLocation resourceLocation) {
                        TooltipBuilder locationTooltip = utils.getValueTooltip(utils, resourceLocation);

                        locationTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(getTranslationKey(resourceLocation)), toString(ints)).build());
                        b.add(locationTooltip.build("ali.property.value.id"));
                    }
                });
            });
        }

        return TooltipBuilder.empty();
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
    public static TooltipBuilder getRecipeEntryTooltip(IServerUtils ignoredUtils, Map.Entry<ResourceLocation, Boolean> entry) {
        return TooltipBuilder.keyValue(entry.getKey().toString(), entry.getValue());
    }

    @NotNull
    public static TooltipBuilder getCriterionEntryTooltip(IServerUtils ignoredUtils, Map.Entry<String, Boolean> entry) {
        return TooltipBuilder.keyValue(entry.getKey(), entry.getValue());
    }

    @NotNull
    public static TooltipBuilder getIntRangeEntryTooltip(IServerUtils utils, Map.Entry<String, IntRange> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.limit"));
    }

    @NotNull
    public static TooltipBuilder getMobEffectPredicateEntryTooltip(IServerUtils utils, Map.Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue().amplifier).build("ali.property.value.amplifier"))
                .add(utils.getValueTooltip(utils, entry.getValue().duration).build("ali.property.value.duration"))
                .add(utils.getValueTooltip(utils, entry.getValue().ambient).build("ali.property.value.is_ambient"))
                .add(utils.getValueTooltip(utils, entry.getValue().visible).build("ali.property.value.is_visible"));
    }

    @NotNull
    public static TooltipBuilder getEnchantmentLevelsEntryTooltip(IServerUtils utils, Map.Entry<Enchantment, NumberProvider> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.levels"));
    }

    @NotNull
    public static TooltipBuilder getMobEffectDurationEntryTooltip(IServerUtils utils, Map.Entry<MobEffect, NumberProvider> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build("ali.property.value.duration"));
    }

    @NotNull
    public static TooltipBuilder getAdvancementEntryTooltip(IServerUtils utils, Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()));
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
