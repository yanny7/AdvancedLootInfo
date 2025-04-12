package com.yanny.ali.plugin.client;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.RangeValue;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TooltipUtils {
    private TooltipUtils() {}

    public static RangeValue getChance(List<LootItemCondition> conditions, float chance) {
        RangeValue value = new RangeValue(chance);
        List<LootItemCondition> list = conditions.stream().filter((f) -> f instanceof LootItemRandomChanceWithLootingCondition).toList();

        for (LootItemCondition c : list) {
            LootItemRandomChanceWithLootingCondition condition = (LootItemRandomChanceWithLootingCondition) c;
            value.multiply(condition.percent);
        }

        list = conditions.stream().filter((f) -> f instanceof LootItemRandomChanceCondition).toList();

        for (LootItemCondition c : list) {
            LootItemRandomChanceCondition condition = (LootItemRandomChanceCondition) c;
            value.multiply(condition.probability);
        }

        list = conditions.stream().filter((f) -> f instanceof BonusLevelTableCondition).toList();

        for (LootItemCondition c : list) {
            BonusLevelTableCondition condition = (BonusLevelTableCondition) c;
            value.set(new RangeValue(condition.values[0]));
        }

        return value.multiply(100);
    }

    @Nullable
    @Unmodifiable
    public static Pair<Enchantment, Map<Integer, RangeValue>> getBonusChance(List<LootItemCondition> conditions, float chance) {
        Map<Integer, RangeValue> bonusChance = new HashMap<>();

        List<LootItemCondition> list = conditions.stream().filter((f) -> f instanceof LootItemRandomChanceWithLootingCondition).toList();

        for (LootItemCondition c : list) {
            LootItemRandomChanceWithLootingCondition condition = (LootItemRandomChanceWithLootingCondition) c;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(chance * (condition.percent + level * condition.lootingMultiplier));
                bonusChance.put(level, value.multiply(100));
            }

            return new Pair<>(Enchantments.MOB_LOOTING, bonusChance);
        }

        list = conditions.stream().filter((f) -> f instanceof BonusLevelTableCondition).toList();

        for (LootItemCondition c : list) {
            BonusLevelTableCondition condition = (BonusLevelTableCondition) c;

            for (int level = 1; level < condition.values.length; level++) {
                RangeValue value = new RangeValue(condition.values[level]);
                bonusChance.put(level, value.multiply(100));
            }

            return new Pair<>(condition.enchantment, bonusChance);
        }

        return null;
    }

    @NotNull
    public static RangeValue getCount(IClientUtils utils, List<LootItemFunction> functions) {
        RangeValue value = new RangeValue();

        functions.stream().filter((f) -> f instanceof SetItemCountFunction).forEach((f) -> {
            SetItemCountFunction function = (SetItemCountFunction) f;

            if (function.add) {
                value.add(utils.convertNumber(utils, function.value));
            } else {
                value.set(utils.convertNumber(utils, function.value));
            }

        });

        functions.stream().filter((f) -> f instanceof ApplyBonusCount).forEach((f) ->
                calculateCount((ApplyBonusCount) f, value, 0));

        functions.stream().filter((f) -> f instanceof LimitCount).forEach((f) -> {
            LimitCount function = (LimitCount) f;

            value.clamp(utils.convertNumber(utils, function.limiter.min), utils.convertNumber(utils, function.limiter.max));
        });

        return value;
    }

    @Nullable
    @Unmodifiable
    public static Pair<Enchantment, Map<Integer, RangeValue>> getBonusCount(IClientUtils utils, List<LootItemFunction> functions, RangeValue count) {
        Map<Integer, RangeValue> bonusCount = new HashMap<>();

        List<LootItemFunction> list = functions.stream().filter((f) -> f instanceof ApplyBonusCount).toList();

        for (LootItemFunction f : list) {
            ApplyBonusCount function = (ApplyBonusCount) f;
            Enchantment enchantment = function.enchantment;

            for (int level = 1; level < enchantment.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);

                calculateCount(function, value, level);
                bonusCount.put(level, value);
            }

            return new Pair<>(enchantment, bonusCount);
        }

        list = functions.stream().filter((f) -> f instanceof LootingEnchantFunction).toList();

        for (LootItemFunction f : list) {
            LootingEnchantFunction function = (LootingEnchantFunction) f;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                value.addMax(new RangeValue(utils.convertNumber(utils, function.value)).multiply(level));
                bonusCount.put(level, value);
            }

            return new Pair<>(Enchantments.MOB_LOOTING, bonusCount);
        }

        return null;
    }

    private static void calculateCount(ApplyBonusCount function, RangeValue value, int level) {
        if (function.formula instanceof ApplyBonusCount.OreDrops) {
            if (level > 0) {
                value.multiplyMax(level + 1);
            }
        } else if (function.formula instanceof ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCount) {
            value.addMax(binomialWithBonusCount.extraRounds + level);
        } else if (function.formula instanceof ApplyBonusCount.UniformBonusCount uniformBonusCount) {
            if (level > 0) {
                value.addMax(uniformBonusCount.bonusMultiplier * level);
            }
        }
    }
}
