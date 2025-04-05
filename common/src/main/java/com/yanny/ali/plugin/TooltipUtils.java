package com.yanny.ali.plugin;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.IUtils;
import com.yanny.ali.api.RangeValue;
import net.minecraft.core.Holder;
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

import java.util.*;

public class TooltipUtils {
    private TooltipUtils() {}

    public static RangeValue getChance(List<LootItemCondition> conditions, float chance) {
        RangeValue value = new RangeValue(chance);
        List<LootItemCondition> list = conditions.stream().filter((f) -> f instanceof LootItemRandomChanceWithLootingCondition).toList();

        for (LootItemCondition c : list) {
            LootItemRandomChanceWithLootingCondition condition = (LootItemRandomChanceWithLootingCondition) c;
            value.multiply(condition.percent());
        }

        list = conditions.stream().filter((f) -> f instanceof LootItemRandomChanceCondition).toList();

        for (LootItemCondition c : list) {
            LootItemRandomChanceCondition condition = (LootItemRandomChanceCondition) c;
            value.multiply(condition.probability());
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

        List<LootItemCondition> list = conditions.stream().filter((f) -> f instanceof LootItemRandomChanceWithLootingCondition).toList();

        for (LootItemCondition c : list) {
            LootItemRandomChanceWithLootingCondition condition = (LootItemRandomChanceWithLootingCondition) c;

            for (int level = 1; level < Enchantments.LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(chance * (condition.percent() + level * condition.lootingMultiplier()));
                bonusChance.put(level, value.multiply(100));
            }

            return Optional.of(new Pair<>(Holder.direct(Enchantments.LOOTING), bonusChance));
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
    public static Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> getBonusCount(IUtils utils, List<LootItemFunction> functions, RangeValue count) {
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

        list = functions.stream().filter((f) -> f instanceof LootingEnchantFunction).toList();

        for (LootItemFunction f : list) {
            LootingEnchantFunction function = (LootingEnchantFunction) f;

            for (int level = 1; level < Enchantments.LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                value.addMax(new RangeValue(utils.convertNumber(utils, function.value)).multiply(level));
                bonusCount.put(level, value);
            }

            return Optional.of(new Pair<>(Holder.direct(Enchantments.LOOTING), bonusCount));
        }

        return Optional.empty();
    }

    @NotNull
    public static List<Item> collectItems(IUtils utils, LootTable lootTable) {
        List<Item> items = new LinkedList<>();

        for (LootPool pool : lootTable.pools) {
            for (LootPoolEntryContainer entry : pool.entries) {
                items.addAll(utils.collectItems(entry.getClass(), utils, entry));
            }
        }

        return items;
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
}
