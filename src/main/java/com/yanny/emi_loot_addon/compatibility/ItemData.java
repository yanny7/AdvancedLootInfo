package com.yanny.emi_loot_addon.compatibility;

import com.yanny.emi_loot_addon.network.*;
import com.yanny.emi_loot_addon.network.condition.ConditionType;
import com.yanny.emi_loot_addon.network.condition.RandomChanceCondition;
import com.yanny.emi_loot_addon.network.condition.RandomChanceWithLootingCondition;
import com.yanny.emi_loot_addon.network.condition.TableBonusCondition;
import com.yanny.emi_loot_addon.network.function.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class ItemData {
    public final Item item;
    public final float rawChance;
    public final RangeValue rawRolls;
    public final RangeValue rawBonusRolls;
    public final RangeValue count;
    public final RangeValue chance;
    public final RangeValue rolls;
    public final List<LootFunction> functions;
    public final List<LootCondition> conditions;
    @Nullable
    public final Map.Entry<Enchantment, Map<Integer, RangeValue>> bonusCount;
    @Nullable
    public final Map.Entry<Enchantment, Map<Integer, RangeValue>> bonusChance;

    public ItemData(ResourceLocation item, float chance, RangeValue rolls, RangeValue bonusRolls, List<LootFunction> functions, List<LootCondition> conditions) {
        this.item = ForgeRegistries.ITEMS.getValue(item);
        this.rawChance = chance;
        this.rawRolls = rolls;
        this.rawBonusRolls = bonusRolls;
        this.functions = functions;
        this.conditions = conditions;
        this.chance = getChance(conditions, chance);
        this.count = getCount(functions);
        this.rolls = getRolls(rolls, bonusRolls);
        this.bonusCount = getBonusCount(functions, this.count);
        this.bonusChance = getBonusChance(conditions, chance);
    }

    @Nullable
    private static Map.Entry<Enchantment, Map<Integer, RangeValue>> getBonusChance(List<LootCondition> conditions, float chance) {
        Map<Integer, RangeValue> bonusChance = new HashMap<>();

        List<LootCondition> list = conditions.stream().filter((f) -> f.type == ConditionType.RANDOM_CHANCE_WITH_LOOTING).toList();

        for (LootCondition c : list) {
            RandomChanceWithLootingCondition condition = (RandomChanceWithLootingCondition) c;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(chance * (condition.percent + level * condition.multiplier));
                bonusChance.put(level, value.multiply(100));
            }

            return Map.entry(Enchantments.MOB_LOOTING, bonusChance);
        }

        list = conditions.stream().filter((f) -> f.type == ConditionType.TABLE_BONUS).toList();

        for (LootCondition c : list) {
            TableBonusCondition condition = (TableBonusCondition) c;
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(condition.location);

            if (enchantment != null) {
                for (int level = 1; level < condition.values.length; level++) {
                    RangeValue value = new RangeValue(condition.values[level]);
                    bonusChance.put(level, value.multiply(100));
                }

                return Map.entry(enchantment, bonusChance);
            }
        }

        return null;
    }

    private static RangeValue getChance(List<LootCondition> conditions, float chance) {
        RangeValue value = new RangeValue(chance);
        List<LootCondition> list = conditions.stream().filter((f) -> f.type == ConditionType.RANDOM_CHANCE_WITH_LOOTING).toList();

        for (LootCondition c : list) {
            RandomChanceWithLootingCondition condition = (RandomChanceWithLootingCondition) c;
            value.multiply(condition.percent);
        }

        list = conditions.stream().filter((f) -> f.type == ConditionType.RANDOM_CHANCE).toList();

        for (LootCondition c : list) {
            RandomChanceCondition condition = (RandomChanceCondition) c;
            value.multiply(condition.probability);
        }

        list = conditions.stream().filter((f) -> f.type == ConditionType.TABLE_BONUS).toList();

        for (LootCondition c : list) {
            TableBonusCondition condition = (TableBonusCondition) c;
            value.set(new RangeValue(condition.values[0]));
        }

        return value.multiply(100);
    }

    @Nullable
    private static Map.Entry<Enchantment, Map<Integer, RangeValue>> getBonusCount(List<LootFunction> functions, RangeValue count) {
        Map<Integer, RangeValue> bonusCount = new HashMap<>();

        List<LootFunction> list = functions.stream().filter((f) -> f.type == FunctionType.APPLY_BONUS).toList();

        for (LootFunction f : list) {
            ApplyBonusFunction function = (ApplyBonusFunction) f;
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(function.enchantment);

            if (enchantment != null) {
                for (int level = 1; level < enchantment.getMaxLevel() + 1; level++) {
                    RangeValue value = new RangeValue(count);
                    function.formula.calculateCount(value, level);
                    bonusCount.put(level, value);
                }

                return Map.entry(enchantment, bonusCount);
            }
        }

        list = functions.stream().filter((f) -> f.type == FunctionType.LOOTING_ENCHANT).toList();

        for (LootFunction f : list) {
            LootingEnchantFunction function = (LootingEnchantFunction) f;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                value.addMax(new RangeValue(function.value).multiply(level));
                bonusCount.put(level, value);
            }

            return Map.entry(Enchantments.MOB_LOOTING, bonusCount);
        }

        return null;
    }

    @NotNull
    private static RangeValue getCount(List<LootFunction> functions) {
        RangeValue value = new RangeValue();

        functions.stream().filter((f) -> f.type == FunctionType.SET_COUNT).forEach((f) -> {
            SetCountFunction function = (SetCountFunction) f;

            if (function.add) {
                value.add(function.count);
            } else {
                value.set(function.count);
            }

        });

        functions.stream().filter((f) -> f.type == FunctionType.APPLY_BONUS).forEach((f) -> {
            ((ApplyBonusFunction) f).formula.calculateCount(value, 0);
        });

        functions.stream().filter((f) -> f.type == FunctionType.LIMIT_COUNT).forEach((f) -> {
            LimitCountFunction function = (LimitCountFunction) f;

            value.clamp(function.min, function.max);
        });

        return value;
    }

    private static RangeValue getRolls(RangeValue rolls, RangeValue bonusRolls) {
        if (bonusRolls.min() > 0 || bonusRolls.max() > 0) {
            return new RangeValue(bonusRolls).add(rolls);
        } else {
            return rolls;
        }
    }

    @NotNull
    public static List<ItemData> parse(LootGroup message) {
        return parse(message, new RangeValue(), new RangeValue(), new LinkedList<>(message.functions), new LinkedList<>(message.conditions));
    }

    @NotNull
    private static List<ItemData> parse(LootGroup message, RangeValue rolls, RangeValue bonusRolls, List<LootFunction> functions, List<LootCondition> conditions) {
        List<ItemData> list = new LinkedList<>();

        for (LootEntry entry : message.entries()) {
            List<LootFunction> allFunctions = Stream.concat(functions.stream(), entry.functions.stream()).toList();
            List<LootCondition> allConditions = Stream.concat(conditions.stream(), entry.conditions.stream()).toList();

            switch (entry.getType()) {
                case INFO -> list.add(new ItemData(((LootInfo) entry).item, ((LootInfo) entry).chance, rolls, bonusRolls, allFunctions, allConditions));
                case GROUP -> list.addAll(parse((LootGroup) entry, rolls, bonusRolls, allFunctions, allConditions));
                case POOL -> list.addAll(parse((LootPoolEntry) entry, ((LootPoolEntry) entry).rolls, ((LootPoolEntry) entry).bonusRolls, allFunctions, allConditions));
            }
        }

        return list;
    }
}
