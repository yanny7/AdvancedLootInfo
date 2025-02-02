package com.yanny.advanced_loot_info.plugin.widget;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.RangeValue;
import com.yanny.advanced_loot_info.plugin.condition.RandomChanceCondition;
import com.yanny.advanced_loot_info.plugin.condition.RandomChanceWithLootingCondition;
import com.yanny.advanced_loot_info.plugin.condition.TableBonusCondition;
import com.yanny.advanced_loot_info.plugin.entry.SingletonEntry;
import com.yanny.advanced_loot_info.plugin.function.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class WidgetUtils {
    @NotNull
    @Unmodifiable
    public static List<Component> getQuality(SingletonEntry entry) {
        if (entry.quality != 0) {
            return List.of(translatable("emi.description.advanced_loot_info.quality", entry.quality));
        }

        return List.of();
    }

    @NotNull
    public static Component getCount(RangeValue count) {
        return translatable("emi.description.advanced_loot_info.count", count);
    }

    @NotNull
    public static Component getChance(RangeValue chance) {
        return translatable("emi.description.advanced_loot_info.chance", value(chance, "%"));
    }

    @NotNull
    public static List<Component> getBonusChance(@Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance) {
        List<Component> components = new LinkedList<>();

        if (bonusChance != null) {
            bonusChance.getSecond().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.advanced_loot_info.chance_bonus",
                    value(value, "%"),
                    Component.translatable(bonusChance.getFirst().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getBonusCount(@Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount) {
        List<Component> components = new LinkedList<>();

        if (bonusCount != null) {
            bonusCount.getSecond().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.advanced_loot_info.count_bonus",
                    value,
                    Component.translatable(bonusCount.getFirst().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getConditions(List<ILootCondition> conditions, int pad) {
        List<Component> components = new LinkedList<>();

        conditions.forEach((condition) -> components.addAll(condition.getTooltip(pad)));

        return components;
    }

    @NotNull
    public static List<Component> getFunctions(List<ILootFunction> functions, int pad) {
        List<Component> components = new LinkedList<>();

        functions.forEach((function) -> {
            components.addAll(function.getTooltip(pad));

            if (function instanceof LootConditionalFunction conditionalFunction) {
                components.addAll(getConditionalFunction(conditionalFunction, pad + 1));
            }
        });

        return components;
    }

    @NotNull
    private static List<Component> getConditionalFunction(LootConditionalFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        if (!function.conditions.isEmpty()) {
            components.add(pad(pad, translatable("emi.property.function.conditions")));
            components.addAll(getConditions(function.conditions, pad + 1));
        }

        return components;
    }

    public static RangeValue getChance(List<ILootCondition> conditions, float chance) {
        RangeValue value = new RangeValue(chance);
        List<ILootCondition> list = conditions.stream().filter((f) -> f instanceof RandomChanceWithLootingCondition).toList();

        for (ILootCondition c : list) {
            RandomChanceWithLootingCondition condition = (RandomChanceWithLootingCondition) c;
            value.multiply(condition.percent);
        }

        list = conditions.stream().filter((f) -> f instanceof RandomChanceCondition).toList();

        for (ILootCondition c : list) {
            RandomChanceCondition condition = (RandomChanceCondition) c;
            value.multiply(condition.probability);
        }

        list = conditions.stream().filter((f) -> f instanceof TableBonusCondition).toList();

        for (ILootCondition c : list) {
            TableBonusCondition condition = (TableBonusCondition) c;
            value.set(new RangeValue(condition.values[0]));
        }

        return value.multiply(100);
    }

    @Nullable
    @Unmodifiable
    public static Pair<Enchantment, Map<Integer, RangeValue>> getBonusChance(List<ILootCondition> conditions, float chance) {
        Map<Integer, RangeValue> bonusChance = new HashMap<>();

        List<ILootCondition> list = conditions.stream().filter((f) -> f instanceof RandomChanceWithLootingCondition).toList();

        for (ILootCondition c : list) {
            RandomChanceWithLootingCondition condition = (RandomChanceWithLootingCondition) c;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(chance * (condition.percent + level * condition.multiplier));
                bonusChance.put(level, value.multiply(100));
            }

            return new Pair<>(Enchantments.MOB_LOOTING, bonusChance);
        }

        list = conditions.stream().filter((f) -> f instanceof TableBonusCondition).toList();

        for (ILootCondition c : list) {
            TableBonusCondition condition = (TableBonusCondition) c;
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(condition.location);

            if (enchantment != null) {
                for (int level = 1; level < condition.values.length; level++) {
                    RangeValue value = new RangeValue(condition.values[level]);
                    bonusChance.put(level, value.multiply(100));
                }

                return new Pair<>(enchantment, bonusChance);
            }
        }

        return null;
    }

    @NotNull
    public static RangeValue getCount(List<ILootFunction> functions) {
        RangeValue value = new RangeValue();

        functions.stream().filter((f) -> f instanceof SetCountFunction).forEach((f) -> {
            SetCountFunction function = (SetCountFunction) f;

            if (function.add) {
                value.add(function.count);
            } else {
                value.set(function.count);
            }

        });

        functions.stream().filter((f) -> f instanceof ApplyBonusFunction).forEach((f) -> {
            ((ApplyBonusFunction) f).formula.calculateCount(value, 0);
        });

        functions.stream().filter((f) -> f instanceof LimitCountFunction).forEach((f) -> {
            LimitCountFunction function = (LimitCountFunction) f;

            value.clamp(function.min, function.max);
        });

        return value;
    }

    @Nullable
    @Unmodifiable
    public static Pair<Enchantment, Map<Integer, RangeValue>> getBonusCount(List<ILootFunction> functions, RangeValue count) {
        Map<Integer, RangeValue> bonusCount = new HashMap<>();

        List<ILootFunction> list = functions.stream().filter((f) -> f instanceof ApplyBonusFunction).toList();

        for (ILootFunction f : list) {
            ApplyBonusFunction function = (ApplyBonusFunction) f;
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(function.enchantment);

            if (enchantment != null) {
                for (int level = 1; level < enchantment.getMaxLevel() + 1; level++) {
                    RangeValue value = new RangeValue(count);
                    function.formula.calculateCount(value, level);
                    bonusCount.put(level, value);
                }

                return new Pair<>(enchantment, bonusCount);
            }
        }

        list = functions.stream().filter((f) -> f instanceof LootingEnchantFunction).toList();

        for (ILootFunction f : list) {
            LootingEnchantFunction function = (LootingEnchantFunction) f;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                value.addMax(new RangeValue(function.value).multiply(level));
                bonusCount.put(level, value);
            }

            return new Pair<>(Enchantments.MOB_LOOTING, bonusCount);
        }

        return null;
    }
}
