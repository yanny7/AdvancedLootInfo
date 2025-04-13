package com.yanny.ali.plugin.client;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.RangeValue;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TooltipUtils {
    private TooltipUtils() {}

    public static RangeValue getChance(IClientUtils utils, List<LootItemCondition> conditions, float chance) {
        RangeValue value = new RangeValue(chance);
        List<LootItemCondition> list = conditions.stream().filter((f) -> f instanceof LootItemRandomChanceWithEnchantedBonusCondition).toList();

        for (LootItemCondition c : list) {
            LootItemRandomChanceWithEnchantedBonusCondition condition = (LootItemRandomChanceWithEnchantedBonusCondition) c;
            value.multiply(condition.unenchantedChance());
        }

        list = conditions.stream().filter((f) -> f instanceof LootItemRandomChanceCondition).toList();

        for (LootItemCondition c : list) {
            LootItemRandomChanceCondition condition = (LootItemRandomChanceCondition) c;
            value.multiply(utils.convertNumber(utils, condition.chance()));
        }

        list = conditions.stream().filter((f) -> f instanceof BonusLevelTableCondition).toList();

        for (LootItemCondition c : list) {
            BonusLevelTableCondition condition = (BonusLevelTableCondition) c;
            value.set(new RangeValue(condition.values().getFirst()));
        }

        return value.multiply(100);
    }

    @Unmodifiable
    public static Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> getBonusChance(List<LootItemCondition> conditions, float chance) {
        Map<Integer, RangeValue> bonusChance = new HashMap<>();

        List<LootItemCondition> list = conditions.stream().filter((f) -> f instanceof LootItemRandomChanceWithEnchantedBonusCondition).toList();

        for (LootItemCondition c : list) {
            LootItemRandomChanceWithEnchantedBonusCondition condition = (LootItemRandomChanceWithEnchantedBonusCondition) c;

            for (int level = 1; level < condition.enchantment().value().getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(chance);
                calculateCount(condition.enchantedChance(), value, level);
                bonusChance.put(level, value.multiply(100));
            }

            return Optional.of(new Pair<>(condition.enchantment(), bonusChance));
        }

        list = conditions.stream().filter((f) -> f instanceof BonusLevelTableCondition).toList();

        for (LootItemCondition c : list) {
            BonusLevelTableCondition condition = (BonusLevelTableCondition) c;

            for (int level = 1; level < condition.values().size(); level++) {
                RangeValue value = new RangeValue(condition.values().get(level));
                bonusChance.put(level, value.multiply(100));
            }

            return Optional.of(new Pair<>(condition.enchantment(), bonusChance));
        }

        return Optional.empty();
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

    @Unmodifiable
    public static Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> getBonusCount(IClientUtils utils, List<LootItemFunction> functions, RangeValue count) {
        Map<Integer, RangeValue> bonusCount = new HashMap<>();

        List<LootItemFunction> list = functions.stream().filter((f) -> f instanceof ApplyBonusCount).toList();

        for (LootItemFunction f : list) {
            ApplyBonusCount function = (ApplyBonusCount) f;

            for (int level = 1; level < function.enchantment.value().getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);

                calculateCount(function, value, level);
                bonusCount.put(level, value);
            }

            return Optional.of(new Pair<>(function.enchantment, bonusCount));
        }

        list = functions.stream().filter((f) -> f instanceof EnchantedCountIncreaseFunction).toList();

        for (LootItemFunction f : list) {
            EnchantedCountIncreaseFunction function = (EnchantedCountIncreaseFunction) f;

            for (int level = 1; level < function.enchantment.value().getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                value.addMax(new RangeValue(utils.convertNumber(utils, function.value)).multiply(level));

                if (function.limit > 0) {
                    value.clamp(Integer.MIN_VALUE, function.limit);
                }

                bonusCount.put(level, value);
            }

            return Optional.of(new Pair<>(function.enchantment, bonusCount));
        }

        return Optional.empty();
    }

    private static void calculateCount(ApplyBonusCount function, RangeValue value, int level) {
        switch (function.formula) {
            case ApplyBonusCount.OreDrops ignored -> {
                if (level > 0) {
                    value.multiplyMax(level + 1);
                }
            }
            case ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCount ->
                    value.addMax(binomialWithBonusCount.extraRounds() + level);
            case ApplyBonusCount.UniformBonusCount(int bonusMultiplier) -> {
                if (level > 0) {
                    value.addMax(bonusMultiplier * level);
                }
            }
            default -> {
            }
        }
    }

    private static void calculateCount(LevelBasedValue levelBasedValue, RangeValue v, int level) {
        switch (levelBasedValue) {
            case LevelBasedValue.Constant(float value) -> v.multiply(value);
            case LevelBasedValue.Clamped(LevelBasedValue value, float min, float max) -> {
                calculateCount(value, v, level);
                v.clamp(min, max);
            }
            case LevelBasedValue.Fraction(LevelBasedValue numerator, LevelBasedValue denominator) -> {
                RangeValue n = new RangeValue();
                RangeValue d = new RangeValue();
                calculateCount(numerator, n, level);
                calculateCount(denominator, d, level);
                v.multiply(new RangeValue(n.min() / d.max(), n.max() / d.min()));
            }
            case LevelBasedValue.Linear(float base, float perLevelAboveFirst) -> v.multiply(new RangeValue(base).add(perLevelAboveFirst * (level - 1)));
            case LevelBasedValue.LevelsSquared(float added) -> v.multiply(added + Mth.square(level));
            case LevelBasedValue.Lookup(List<Float> values, LevelBasedValue fallback) -> {
                if (level < values.size()) {
                    v.multiply(values.get(level));
                } else {
                    calculateCount(fallback, v, level);
                }
            }
            default -> v.set(new RangeValue(false, true));
        };
    }
}
