package com.yanny.ali.plugin;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.IUtils;
import com.yanny.ali.api.RangeValue;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            value.set(new RangeValue(condition.values.get(0)));
        }

        return value.multiply(100);
    }

    @Unmodifiable
    public static Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> getBonusChance(List<LootItemCondition> conditions, float chance) {
        Map<Integer, RangeValue> bonusChance = new HashMap<>();

        List<LootItemCondition> list = conditions.stream().filter((f) -> f instanceof LootItemRandomChanceWithLootingCondition).toList();

        for (LootItemCondition c : list) {
            LootItemRandomChanceWithLootingCondition condition = (LootItemRandomChanceWithLootingCondition) c;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(chance * (condition.percent + level * condition.lootingMultiplier));
                bonusChance.put(level, value.multiply(100));
            }

            return Optional.of(new Pair<>(Holder.direct(Enchantments.MOB_LOOTING), bonusChance));
        }

        list = conditions.stream().filter((f) -> f instanceof BonusLevelTableCondition).toList();

        for (LootItemCondition c : list) {
            BonusLevelTableCondition condition = (BonusLevelTableCondition) c;

            if (((TableBonusAliCondition) c).enchantment != null) {
                for (int level = 1; level < condition.values.size(); level++) {
                    RangeValue value = new RangeValue(condition.values.get(level));
                    bonusChance.put(level, value.multiply(100));
                }

                return Optional.of(new Pair<>(condition.enchantment, bonusChance));
            }
        }

        return Optional.empty();
    }

    @NotNull
    public static RangeValue getCount(IUtils utils, List<LootItemFunction> functions) {
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
    public static Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> getBonusCount(List<LootItemFunction> functions, RangeValue count) {
        Map<Integer, RangeValue> bonusCount = new HashMap<>();

        List<LootItemFunction> list = functions.stream().filter((f) -> f instanceof ApplyBonusCount).toList();

        for (LootItemFunction f : list) {
            ApplyBonusCount function = (ApplyBonusCount) f;
            Enchantment enchantment = function.enchantment;

            for (int level = 1; level < enchantment.value().getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);

                calculateCount(function, value, level);
                bonusCount.put(level, value);
            }

            return Optional.of(new Pair<>(enchantment, bonusCount));
        }

        list = functions.stream().filter((f) -> f instanceof LootingEnchantFunction).toList();

        for (LootItemFunction f : list) {
            LootingEnchantFunction function = (LootingEnchantFunction) f;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                value.addMax(new RangeValue(utils.convertNumber(utils, function.value)).multiply(level));
                bonusCount.put(level, value);
            }

            return Optional.of(new Pair<>(Holder.direct(Enchantments.MOB_LOOTING), bonusCount));
        }

        return Optional.empty();
    }

    @NotNull
    public static List<Item> collectItems(IUtils utils, LootTable lootTable) {
        List<Item> items = new LinkedList<>();

        for (LootPool pool : utils.getLootPools(lootTable)) {
            for (LootPoolEntryContainer entry : pool.entries) {
                items.addAll(utils.collectItems(entry.getClass(), utils, entry));
            }
        }

        return items;
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
