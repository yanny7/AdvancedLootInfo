package com.yanny.ali.plugin;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.condition.RandomChanceAliCondition;
import com.yanny.ali.plugin.condition.RandomChanceWithLootingAliCondition;
import com.yanny.ali.plugin.condition.TableBonusAliCondition;
import com.yanny.ali.plugin.function.ApplyBonusAliFunction;
import com.yanny.ali.plugin.function.LimitCountAliFunction;
import com.yanny.ali.plugin.function.LootingEnchantAliFunction;
import com.yanny.ali.plugin.function.SetCountAliFunction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TooltipUtils {
    private TooltipUtils() {}

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
            value.set(new RangeValue(condition.values.getFirst()));
        }

        return value.multiply(100);
    }

    @Unmodifiable
    public static Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> getBonusChance(List<ILootCondition> conditions, float chance) {
        Map<Integer, RangeValue> bonusChance = new HashMap<>();

        List<ILootCondition> list = conditions.stream().filter((f) -> f instanceof RandomChanceWithLootingAliCondition).toList();

        for (ILootCondition c : list) {
            RandomChanceWithLootingAliCondition condition = (RandomChanceWithLootingAliCondition) c;

            for (int level = 1; level < Enchantments.LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(chance * (condition.percent + level * condition.multiplier));
                bonusChance.put(level, value.multiply(100));
            }

            return Optional.of(new Pair<>(Holder.direct(Enchantments.LOOTING), bonusChance));
        }

        list = conditions.stream().filter((f) -> f instanceof TableBonusAliCondition).toList();

        for (ILootCondition c : list) {
            TableBonusAliCondition condition = (TableBonusAliCondition) c;

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

    @Unmodifiable
    public static Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> getBonusCount(List<ILootFunction> functions, RangeValue count) {
        Map<Integer, RangeValue> bonusCount = new HashMap<>();

        List<ILootFunction> list = functions.stream().filter((f) -> f instanceof ApplyBonusAliFunction).toList();

        for (ILootFunction f : list) {
            ApplyBonusAliFunction function = (ApplyBonusAliFunction) f;
            Holder<Enchantment> enchantment = function.enchantment;

            for (int level = 1; level < enchantment.value().getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                function.calculateCount(value, level);
                bonusCount.put(level, value);
            }

            return Optional.of(new Pair<>(enchantment, bonusCount));
        }

        list = functions.stream().filter((f) -> f instanceof LootingEnchantAliFunction).toList();

        for (ILootFunction f : list) {
            LootingEnchantAliFunction function = (LootingEnchantAliFunction) f;

            for (int level = 1; level < Enchantments.LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                value.addMax(new RangeValue(function.value).multiply(level));
                bonusCount.put(level, value);
            }

            return Optional.of(new Pair<>(Holder.direct(Enchantments.LOOTING), bonusCount));
        }

        return Optional.empty();
    }
}
