package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.RangeValue;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TooltipUtils {
    @NotNull
    public static Map<Enchantment, Map<Integer, RangeValue>> getChance(IServerUtils utils, List<LootItemCondition> conditions, float rawChance) {
        Map<Enchantment, Map<Integer, RangeValue>> chance = new LinkedHashMap<>();

        chance.put(null, Map.of(0, new RangeValue(rawChance * 100)));

        for (LootItemCondition condition : conditions) {
            utils.applyChanceModifier(utils, condition, chance);
        }

        return chance;
    }

    @NotNull
    public static Map<Enchantment, Map<Integer, RangeValue>> getCount(IServerUtils utils, List<LootItemFunction> functions) {
        Map<Enchantment, Map<Integer, RangeValue>> count = new LinkedHashMap<>();

        count.put(null, Map.of(0, new RangeValue()));

        for (LootItemFunction function : functions) {
            utils.applyCountModifier(utils, function, count);
        }

        return count;
    }

    public static ItemStack getItemStack(IServerUtils utils, ItemStack itemStack, List<LootItemFunction> functions) {
        for (LootItemFunction function : functions) {
            itemStack = utils.applyItemStackModifier(utils, function, itemStack);
        }

        return itemStack;
    }

    public static void applyRandomChance(IServerUtils utils, LootItemRandomChanceCondition condition, Map<Enchantment, Map<Integer, RangeValue>> chance) {
        for (Map.Entry<Enchantment, Map<Integer, RangeValue>> chanceMap : chance.entrySet()) {
            for (Map.Entry<Integer, RangeValue> levelEntry : chanceMap.getValue().entrySet()) {
                levelEntry.getValue().multiply(condition.probability);
            }
        }
    }

    public static void applyRandomChanceWithLooting(IServerUtils utils, LootItemRandomChanceWithLootingCondition condition, Map<Enchantment, Map<Integer, RangeValue>> chance) {
        Enchantment enchantment = Enchantments.MOB_LOOTING;

        if (chance.containsKey(enchantment)) {
            chance.get(null).get(0).multiply(condition.percent);

            for (Map.Entry<Integer, RangeValue> entry : chance.get(enchantment).entrySet()) {
                entry.getValue().multiply(condition.percent + entry.getKey() * condition.lootingMultiplier);
            }
        } else {
            RangeValue baseChance = new RangeValue(chance.get(null).get(0));
            Map<Integer, RangeValue> levelMap = new LinkedHashMap<>();

            chance.get(null).get(0).multiply(condition.percent);
            chance.put(enchantment, levelMap);

            for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                levelMap.put(level, new RangeValue(baseChance).multiply(condition.percent + level * condition.lootingMultiplier));
            }
        }
    }

    public static void applyTableBonus(IServerUtils utils, BonusLevelTableCondition condition, Map<Enchantment, Map<Integer, RangeValue>> chance) {
        Enchantment enchantment = condition.enchantment;

        if (condition.values.length > 0) {
            if (chance.containsKey(enchantment)) {
                chance.get(null).get(0).multiply(condition.values[0]);

                if (condition.values.length > 1) {
                    for (Map.Entry<Integer, RangeValue> entry : chance.get(enchantment).entrySet()) {
                        Integer level = entry.getKey();

                        if (level < condition.values.length) {
                            entry.getValue().multiply(condition.values[level]);
                        }
                    }
                }
            } else {
                RangeValue baseChance = new RangeValue(chance.get(null).get(0));
                Map<Integer, RangeValue> levelMap = new LinkedHashMap<>();

                chance.get(null).get(0).multiply(condition.values[0]);
                chance.put(enchantment, levelMap);

                for (int level = 1; level <= enchantment.getMaxLevel() && level < condition.values.length; level++) {
                    levelMap.put(level, new RangeValue(baseChance).multiply(condition.values[level]));
                }
            }
        }
    }

    public static void applySetCount(IServerUtils utils, SetItemCountFunction function, Map<Enchantment, Map<Integer, RangeValue>> count) {
        if (function.predicates.length == 0) {
            for (Map.Entry<Enchantment, Map<Integer, RangeValue>> chanceMap : count.entrySet()) {
                for (Map.Entry<Integer, RangeValue> levelEntry : chanceMap.getValue().entrySet()) {
                    if (function.add) {
                        levelEntry.getValue().add(utils.convertNumber(utils, function.value));
                    } else {
                        levelEntry.getValue().set(utils.convertNumber(utils, function.value));
                    }
                }
            }
        }
    }

    public static void applyBonus(IServerUtils utils, ApplyBonusCount function, Map<Enchantment, Map<Integer, RangeValue>> count) {
        if (function.predicates.length == 0) {
            Enchantment enchantment = function.enchantment;

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

                for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                    RangeValue value = new RangeValue(baseCount);

                    calculateCount(function, value, level);
                    levelMap.put(level, value);
                }
            }
        }
    }

    public static void applyLimitCount(IServerUtils utils, LimitCount function, Map<Enchantment, Map<Integer, RangeValue>> bonusCount) {
        if (function.predicates.length == 0) {
            for (Map.Entry<Enchantment, Map<Integer, RangeValue>> entry : bonusCount.entrySet()) {
                for (Map.Entry<Integer, RangeValue> mapEntry : entry.getValue().entrySet()) {
                    RangeValue value = mapEntry.getValue();
                    value.clamp(utils.convertNumber(utils, function.limiter.min), utils.convertNumber(utils, function.limiter.max));
                }
            }
        }
    }

    public static void applyLootingEnchant(IServerUtils utils, LootingEnchantFunction function, Map<Enchantment, Map<Integer, RangeValue>> count) {
        if (function.predicates.length == 0) {
            Enchantment enchantment = Enchantments.MOB_LOOTING;

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

                for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                    RangeValue value = new RangeValue(baseCount).add(utils.convertNumber(utils, function.value).multiply(level));

                    if (function.limit > 0) {
                        value.clamp(new RangeValue(false, true), new RangeValue(function.limit));
                    }

                    levelMap.put(level, value);
                }
            }
        }
    }

    @NotNull
    public static ItemStack applyEnchantRandomlyItemStackModifier(IServerUtils utils, EnchantRandomlyFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.length == 0) {
            boolean isBook = itemStack.is(Items.BOOK);
            ItemStack finalItemStack = itemStack;
            List<Enchantment> enchantments = function.enchantments;

            if (enchantments.isEmpty()) {
                enchantments = BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isDiscoverable).filter((enchantment) -> isBook || enchantment.canEnchant(finalItemStack)).toList();
            }

            if (enchantments.size() == 1 && enchantments.get(0).getMinLevel() == enchantments.get(0).getMaxLevel()) {
                itemStack.enchant(enchantments.get(0), enchantments.get(0).getMaxLevel());
            } else if (isBook) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            }
        }

        return itemStack;
    }

    @NotNull
    public static ItemStack applyEnchantWithLevelsItemStackModifier(IServerUtils utils, EnchantWithLevelsFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.length == 0) {
            if (itemStack.is(Items.BOOK)) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            }
        }

        return itemStack;
    }

    public static ItemStack applySetAttributesItemStackModifier(IServerUtils utils, SetAttributesFunction function, ItemStack itemStack) {
        if (function.predicates.length == 0) {
            for (SetAttributesFunction.Modifier modifier : function.modifiers) {
                UUID id = modifier.id;

                if (id == null) {
                    id = UUID.randomUUID();
                }

                if (modifier.slots.length == 1 && modifier.amount.getType() == NumberProviders.CONSTANT) {
                    EquipmentSlot equipmentSlot = Util.getRandom(modifier.slots, RandomSource.create());
                    ConstantValue value = (ConstantValue) modifier.amount;

                    itemStack.addAttributeModifier(modifier.attribute, new AttributeModifier(id, modifier.name, value.getFloat(null), modifier.operation), equipmentSlot);
                }
            }
        }

        return itemStack;
    }

    public static ItemStack applySetNameItemStackModifier(IServerUtils utils, SetNameFunction function, ItemStack itemStack) {
        if (function.predicates.length == 0) {
            itemStack.setHoverName(function.name);
        }

        return itemStack;
    }

    public static ItemStack applyItemStackModifier(IServerUtils utils, LootItemFunction function, ItemStack itemStack) {
        if (function instanceof LootItemConditionalFunction conditional && conditional.predicates.length > 0) {
            return itemStack;
        }

        itemStack = function.apply(itemStack, null);
        return itemStack;
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
