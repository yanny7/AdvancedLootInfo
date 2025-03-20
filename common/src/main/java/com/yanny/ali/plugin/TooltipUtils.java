package com.yanny.ali.plugin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.condition.RandomChanceAliCondition;
import com.yanny.ali.plugin.condition.RandomChanceWithLootingAliCondition;
import com.yanny.ali.plugin.condition.TableBonusAliCondition;
import com.yanny.ali.plugin.entry.SingletonEntry;
import com.yanny.ali.plugin.function.*;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class TooltipUtils {
    private static final ChatFormatting TEXT_STYLE = ChatFormatting.GOLD;
    private static final ChatFormatting PARAM_STYLE = ChatFormatting.AQUA;

    private TooltipUtils() {}

    @NotNull
    public static List<Component> getConditions(List<ILootCondition> conditions, int pad) {
        List<Component> components = new LinkedList<>();

        if (!conditions.isEmpty()) {
            components.add(translatable("ali.util.advanced_loot_info.delimiter.conditions"));
            conditions.forEach((condition) -> components.addAll(condition.getTooltip(pad)));
        }

        return components;
    }

    @NotNull
    public static List<Component> getFunctions(List<ILootFunction> functions, int pad) {
        List<Component> components = new LinkedList<>();

        if (!functions.isEmpty()) {
            components.add(translatable("ali.util.advanced_loot_info.delimiter.functions"));
            functions.forEach((function) -> {
                components.addAll(function.getTooltip(pad));

                if (function instanceof LootConditionalAliFunction conditionalFunction) {
                    components.addAll(getConditionalFunction(conditionalFunction, pad + 1));
                }
            });
        }

        return components;
    }

    @NotNull
    private static List<Component> getConditionalFunction(LootConditionalAliFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        if (!function.conditions.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.condition")));
            function.conditions.forEach((condition) -> components.addAll(condition.getTooltip(pad + 1)));
        }

        return components;
    }

    @NotNull
    @Unmodifiable
    public static List<Component> getQuality(SingletonEntry entry) {
        if (entry.quality != 0) {
            return List.of(translatable("ali.description.quality", entry.quality));
        }

        return List.of();
    }

    @NotNull
    public static Component getCount(RangeValue count) {
        return translatable("ali.description.count", value(count));
    }

    @NotNull
    public static Component getChance(RangeValue chance) {
        return translatable("ali.description.chance", value(chance, "%"));
    }

    @NotNull
    public static List<Component> getBonusChance(@Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance) {
        List<Component> components = new LinkedList<>();

        if (bonusChance != null) {
            bonusChance.getSecond().entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach((entry) ->
                    components.add(pad(1, translatable(
                            "ali.description.chance_bonus",
                            value(entry.getValue(), "%"),
                            Component.translatable(bonusChance.getFirst().getDescriptionId()),
                            Component.translatable("enchantment.level." + entry.getKey())
                    )))
            );
        }

        return components;
    }

    @NotNull
    public static List<Component> getBonusCount(@Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount) {
        List<Component> components = new LinkedList<>();

        if (bonusCount != null) {
            bonusCount.getSecond().entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach((entry) ->
                    components.add(pad(1, translatable(
                            "ali.description.count_bonus",
                            value(entry.getValue()),
                            Component.translatable(bonusCount.getFirst().getDescriptionId()),
                            Component.translatable("enchantment.level." + entry.getKey())
                    )))
            );
        }

        return components;
    }

    public static RangeValue getChance(List<ILootCondition> conditions, float chance) {
        RangeValue value = new RangeValue(chance);
        List<ILootCondition> list = conditions.stream().filter((f) -> f instanceof RandomChanceWithLootingAliCondition).toList();

        for (ILootCondition c : list) {
            RandomChanceWithLootingAliCondition condition = (RandomChanceWithLootingAliCondition) c;
            value.multiply(condition.percent);
        }

        list = conditions.stream().filter((f) -> f instanceof RandomChanceAliCondition).toList();

        for (ILootCondition c : list) {
            RandomChanceAliCondition condition = (RandomChanceAliCondition) c;
            value.multiply(condition.probability);
        }

        list = conditions.stream().filter((f) -> f instanceof TableBonusAliCondition).toList();

        for (ILootCondition c : list) {
            TableBonusAliCondition condition = (TableBonusAliCondition) c;
            value.set(new RangeValue(condition.values.get(0)));
        }

        return value.multiply(100);
    }

    @Nullable
    @Unmodifiable
    public static Pair<Enchantment, Map<Integer, RangeValue>> getBonusChance(List<ILootCondition> conditions, float chance) {
        Map<Integer, RangeValue> bonusChance = new HashMap<>();

        List<ILootCondition> list = conditions.stream().filter((f) -> f instanceof RandomChanceWithLootingAliCondition).toList();

        for (ILootCondition c : list) {
            RandomChanceWithLootingAliCondition condition = (RandomChanceWithLootingAliCondition) c;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(chance * (condition.percent + level * condition.multiplier));
                bonusChance.put(level, value.multiply(100));
            }

            return new Pair<>(Enchantments.MOB_LOOTING, bonusChance);
        }

        list = conditions.stream().filter((f) -> f instanceof TableBonusAliCondition).toList();

        for (ILootCondition c : list) {
            TableBonusAliCondition condition = (TableBonusAliCondition) c;

            if (enchantment != null) {
                for (int level = 1; level < condition.values.size(); level++) {
                    RangeValue value = new RangeValue(condition.values.get(level));
                    bonusChance.put(level, value.multiply(100));
                }

                return new Pair<>(condition.enchantment, bonusChance);
            }
        }

        return null;
    }

    @NotNull
    public static RangeValue getCount(List<ILootFunction> functions) {
        RangeValue value = new RangeValue();

        functions.stream().filter((f) -> f instanceof SetCountAliFunction).forEach((f) -> {
            SetCountAliFunction function = (SetCountAliFunction) f;

            if (function.add) {
                value.add(function.count);
            } else {
                value.set(function.count);
            }

        });

        functions.stream().filter((f) -> f instanceof ApplyBonusAliFunction).forEach((f) ->
                ((ApplyBonusAliFunction) f).calculateCount(value, 0));

        functions.stream().filter((f) -> f instanceof LimitCountAliFunction).forEach((f) -> {
            LimitCountAliFunction function = (LimitCountAliFunction) f;

            value.clamp(function.min, function.max);
        });

        return value;
    }

    @Nullable
    @Unmodifiable
    public static Pair<Enchantment, Map<Integer, RangeValue>> getBonusCount(List<ILootFunction> functions, RangeValue count) {
        Map<Integer, RangeValue> bonusCount = new HashMap<>();

        List<ILootFunction> list = functions.stream().filter((f) -> f instanceof ApplyBonusAliFunction).toList();

        for (ILootFunction f : list) {
            ApplyBonusAliFunction function = (ApplyBonusAliFunction) f;
            Enchantment enchantment = function.enchantment.value();

            for (int level = 1; level < enchantment.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                function.calculateCount(value, level);
                bonusCount.put(level, value);
            }

            return new Pair<>(enchantment, bonusCount);
        }

        list = functions.stream().filter((f) -> f instanceof LootingEnchantAliFunction).toList();

        for (ILootFunction f : list) {
            LootingEnchantAliFunction function = (LootingEnchantAliFunction) f;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                value.addMax(new RangeValue(function.value).multiply(level));
                bonusCount.put(level, value);
            }

            return new Pair<>(Enchantments.MOB_LOOTING, bonusCount);
        }

        return null;
    }

    @NotNull
    public static MutableComponent translatableType(String prefix, Enum<?> type, Object... args) {
        return translatable(prefix + "." + type.name().toLowerCase(), args);
    }

    @NotNull
    public static MutableComponent translatableType(String prefix, String type, Object... args) {
        return translatable(prefix + "." + type.toLowerCase(), args);
    }

    @NotNull
    public static MutableComponent translatable(String key, Object... args) {
        return Component.translatable(key, Arrays.stream(args).map((arg) -> {
            if (arg instanceof MutableComponent) {
                return arg;
            } else if (arg != null) {
                return Component.literal(arg.toString());
            } else {
                return Component.literal("null");
            }
        }).toArray()).withStyle(TEXT_STYLE);
    }

    @NotNull
    public static MutableComponent value(Object value) {
        if (value instanceof MutableComponent) {
            return ((MutableComponent) value).withStyle(PARAM_STYLE, ChatFormatting.BOLD);
        } else {
            return Component.literal(value.toString()).withStyle(PARAM_STYLE, ChatFormatting.BOLD);
        }
    }

    @NotNull
    public static MutableComponent value(Object value, String unit) {
        return Component.translatable("ali.util.advanced_loot_info.two_values", value, unit).withStyle(PARAM_STYLE, ChatFormatting.BOLD);
    }

    @NotNull
    public static MutableComponent pair(Object value1, Object value2) {
        return Component.translatable("ali.util.advanced_loot_info.two_values_with_space", value1, value2);
    }

    @NotNull
    public static MutableComponent pad(int count, Object arg) {
        if (count > 0) {
            return pair(Component.translatable("ali.util.advanced_loot_info.pad." + count), arg);
        } else {
            if (arg instanceof MutableComponent mutableComponent) {
                return mutableComponent;
            } else {
                return Component.literal(arg.toString());
            }
        }
    }

    @NotNull
    public static MutableComponent keyValue(Object key, Object value) {
        return translatable("ali.util.advanced_loot_info.key_value", key instanceof MutableComponent ? key : Component.literal(key.toString()), value(value));
    }
}
