package com.yanny.advanced_loot_info.compatibility;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.network.*;
import com.yanny.advanced_loot_info.network.condition.ConditionType;
import com.yanny.advanced_loot_info.network.condition.RandomChanceCondition;
import com.yanny.advanced_loot_info.network.condition.RandomChanceWithLootingCondition;
import com.yanny.advanced_loot_info.network.condition.TableBonusCondition;
import com.yanny.advanced_loot_info.network.function.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
import java.util.stream.Stream;

public final class ItemData {
    public final Item item;
    public final float rawChance;
    public final RangeValue count;
    public final RangeValue chance;
    public final List<LootFunction> functions;
    public final List<LootCondition> conditions;
    @Nullable
    public final Pair<Enchantment, Map<Integer, RangeValue>> bonusCount;
    @Nullable
    public final Pair<Enchantment, Map<Integer, RangeValue>> bonusChance;

    public ItemData(ResourceLocation item, float chance, List<LootFunction> functions, List<LootCondition> conditions) {
        this.item = ForgeRegistries.ITEMS.getValue(item);
        this.rawChance = chance;
        this.functions = functions;
        this.conditions = conditions;
        this.chance = getChance(conditions, chance);
        this.count = getCount(functions);
        this.bonusCount = getBonusCount(functions, this.count);
        this.bonusChance = getBonusChance(conditions, chance);
    }

    @Nullable
    @Unmodifiable
    private static Pair<Enchantment, Map<Integer, RangeValue>> getBonusChance(List<LootCondition> conditions, float chance) {
        Map<Integer, RangeValue> bonusChance = new HashMap<>();

        List<LootCondition> list = conditions.stream().filter((f) -> f.type == ConditionType.RANDOM_CHANCE_WITH_LOOTING).toList();

        for (LootCondition c : list) {
            RandomChanceWithLootingCondition condition = (RandomChanceWithLootingCondition) c;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(chance * (condition.percent + level * condition.multiplier));
                bonusChance.put(level, value.multiply(100));
            }

            return new Pair<>(Enchantments.MOB_LOOTING, bonusChance);
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

                return new Pair<>(enchantment, bonusChance);
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
    @Unmodifiable
    private static Pair<Enchantment, Map<Integer, RangeValue>> getBonusCount(List<LootFunction> functions, RangeValue count) {
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

                return new Pair<>(enchantment, bonusCount);
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

            return new Pair<>(Enchantments.MOB_LOOTING, bonusCount);
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

    @NotNull
    public static ItemGroup parse(LootTableEntry message) {
        List<ItemGroup> groups = new LinkedList<>();

        for (LootEntry entry : message.entries()) {
            List<LootFunction> allFunctions = Stream.concat(message.functions.stream(), entry.functions.stream()).toList();
            List<LootCondition> allConditions = Stream.concat(message.conditions.stream(), entry.conditions.stream()).toList();

            if (entry.getType() == EntryType.POOL) {
                LootPoolEntry poolEntry = (LootPoolEntry) entry;
                groups.add(parse(poolEntry, new LinkedList<>(allFunctions), new LinkedList<>(allConditions)));
            }
        }

        return new ItemGroup(GroupType.ALL, groups);
    }

    @NotNull
    private static ItemGroup parse(LootGroup message, List<LootFunction> functions, List<LootCondition> conditions) {
        List<ItemData> items = new LinkedList<>();
        List<ItemGroup> groups = new LinkedList<>();

        for (LootEntry entry : message.entries()) {
            List<LootFunction> allFunctions = Stream.concat(functions.stream(), entry.functions.stream()).toList();
            List<LootCondition> allConditions = Stream.concat(conditions.stream(), entry.conditions.stream()).toList();

            switch (entry.getType()) {
                case INFO -> items.add(new ItemData(((LootInfo) entry).item, ((LootInfo) entry).chance, allFunctions, allConditions));
                case GROUP -> groups.add(parse((LootGroup) entry, allFunctions, allConditions));
                case POOL -> groups.add(parse((LootPoolEntry) entry, allFunctions, allConditions));
                case TABLE -> groups.add(parse((LootTableEntry) entry, allFunctions, allConditions));
            }
        }

        if (message instanceof LootPoolEntry poolEntry) {
            return new ItemGroup(message.getGroupType(), items, groups, new ItemGroup.RollsHolder(poolEntry.rolls, poolEntry.bonusRolls), null);
        } else {
            return new ItemGroup(message.getGroupType(), items, groups, null, new ItemGroup.WeightHolder(message.getChance(), message.getQuality()));
        }
    }
}
