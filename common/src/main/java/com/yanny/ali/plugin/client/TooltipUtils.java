package com.yanny.ali.plugin.client;

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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TooltipUtils {
    private TooltipUtils() {}

    @NotNull
    public static Map<Holder<Enchantment>, Map<Integer, RangeValue>> getChance(IClientUtils utils, List<LootItemCondition> conditions, float rawChance) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance = new LinkedHashMap<>();

        chance.put(null, Map.of(0, new RangeValue(rawChance * 100)));

        for (LootItemCondition condition : conditions) {
            utils.applyChance(utils, condition, chance);
        }

        return chance;
    }

    @NotNull
    public static Map<Holder<Enchantment>, Map<Integer, RangeValue>> getCount(IClientUtils utils, List<LootItemFunction> functions) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> count = new LinkedHashMap<>();

        count.put(null, Map.of(0, new RangeValue()));

        for (LootItemFunction function : functions) {
            utils.applyCount(utils, function, count);
        }

        return count;
    }

    public static void applyRandomChance(IClientUtils utils, LootItemRandomChanceCondition condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> chanceMap : chance.entrySet()) {
            for (Map.Entry<Integer, RangeValue> levelEntry : chanceMap.getValue().entrySet()) {
                levelEntry.getValue().multiply(utils.convertNumber(utils, condition.chance()));
            }
        }
    }

    public static void applyRandomChanceWithLooting(IClientUtils utils, LootItemRandomChanceWithEnchantedBonusCondition condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        Holder<Enchantment> enchantment = condition.enchantment();

        if (chance.containsKey(enchantment)) {
            chance.get(null).get(0).multiply(condition.unenchantedChance());

            for (Map.Entry<Integer, RangeValue> entry : chance.get(enchantment).entrySet()) {
                RangeValue value = new RangeValue();

                calculateCount(condition.enchantedChance(), value, entry.getKey());
                entry.getValue().multiply(value);
            }
        } else {
            RangeValue baseChance = new RangeValue(chance.get(null).get(0));
            Map<Integer, RangeValue> levelMap = new LinkedHashMap<>();

            chance.get(null).get(0).multiply(condition.unenchantedChance());
            chance.put(enchantment, levelMap);

            for (int level = 1; level <= enchantment.value().getMaxLevel(); level++) {
                RangeValue value = new RangeValue();

                calculateCount(condition.enchantedChance(), value, level);
                levelMap.put(level, new RangeValue(baseChance).multiply(value));
            }
        }
    }

    public static void applyTableBonus(IClientUtils utils, BonusLevelTableCondition condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        Holder<Enchantment> enchantment = condition.enchantment();

        if (!condition.values().isEmpty()) {
            if (chance.containsKey(enchantment)) {
                chance.get(null).get(0).multiply(condition.values().getFirst());

                if (condition.values().size() > 1) {
                    for (Map.Entry<Integer, RangeValue> entry : chance.get(enchantment).entrySet()) {
                        Integer level = entry.getKey();

                        if (level < condition.values().size()) {
                            entry.getValue().multiply(condition.values().get(level));
                        }
                    }
                }
            } else {
                RangeValue baseChance = new RangeValue(chance.get(null).get(0));
                Map<Integer, RangeValue> levelMap = new LinkedHashMap<>();

                chance.get(null).get(0).multiply(condition.values().getFirst());
                chance.put(enchantment, levelMap);

                for (int level = 1; level <= enchantment.value().getMaxLevel() && level < condition.values().size(); level++) {
                    levelMap.put(level, new RangeValue(baseChance).multiply(condition.values().get(level)));
                }
            }
        }
    }

    public static void applySetCount(IClientUtils utils, SetItemCountFunction function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> chanceMap : count.entrySet()) {
            for (Map.Entry<Integer, RangeValue> levelEntry : chanceMap.getValue().entrySet()) {
                if (function.add) {
                    levelEntry.getValue().add(utils.convertNumber(utils, function.value));
                } else {
                    levelEntry.getValue().set(utils.convertNumber(utils, function.value));
                }
            }
        }
    }

    public static void applyBonus(IClientUtils utils, ApplyBonusCount function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        Holder<Enchantment> enchantment = function.enchantment;

        if (count.containsKey(enchantment)) {
            calculateCount(function, count.get(null).get(0), 0);

            for (Map.Entry<Integer, RangeValue> entry : count.get(enchantment).entrySet()) {
                calculateCount(function, entry.getValue(), entry.getKey());
            }
        } else {
            RangeValue baseCount = new RangeValue(count.get(null).get(0));
            Map<Integer, RangeValue> levelMap = new LinkedHashMap<>();

            calculateCount(function, count.get(null).get(0), 0);
            count.put(enchantment, levelMap);

            for (int level = 1; level <= enchantment.value().getMaxLevel(); level++) {
                RangeValue value = new RangeValue(baseCount);

                calculateCount(function, value, level);
                levelMap.put(level, value);
            }
        }
    }

    public static void applyLimitCount(IClientUtils utils, LimitCount function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> bonusCount) {
        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> entry : bonusCount.entrySet()) {
            for (Map.Entry<Integer, RangeValue> mapEntry : entry.getValue().entrySet()) {
                RangeValue value = mapEntry.getValue();
                value.clamp(utils.convertNumber(utils, function.limiter.min), utils.convertNumber(utils, function.limiter.max));
            }
        }
    }

    public static void applyLootingEnchant(IClientUtils utils, EnchantedCountIncreaseFunction function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        Holder<Enchantment> enchantment = function.enchantment;

        if (count.containsKey(enchantment)) {
            for (Map.Entry<Integer, RangeValue> entry : count.get(enchantment).entrySet()) {
                RangeValue value = entry.getValue();

                value.add(utils.convertNumber(utils, function.value).multiply(entry.getKey()));

                if (function.limit > 0) {
                    value.clamp(new RangeValue(false, true), new RangeValue(function.limit));
                }
            }
        } else {
            RangeValue baseCount = new RangeValue(count.get(null).get(0));
            Map<Integer, RangeValue> levelMap = new LinkedHashMap<>();

            count.put(enchantment, levelMap);

            for (int level = 1; level <= enchantment.value().getMaxLevel(); level++) {
                RangeValue value = new RangeValue(baseCount).add(utils.convertNumber(utils, function.value).multiply(level));

                if (function.limit > 0) {
                    value.clamp(new RangeValue(false, true), new RangeValue(function.limit));
                }

                levelMap.put(level, value);
            }
        }
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

    public static void calculateCount(LevelBasedValue levelBasedValue, RangeValue v, int level) {
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
