package com.yanny.ali.plugin.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class TooltipUtils {
    public static ItemStack getItemStack(IServerUtils utils, ItemStack itemStack, List<LootItemFunction> functions) {
        for (LootItemFunction function : functions) {
            itemStack = utils.applyItemStackModifier(utils, function, itemStack);
        }

        return itemStack;
    }

    public static void applyRandomChance(IServerUtils ignoredUtils, LootItemRandomChanceCondition condition, Map<Enchantment, Map<Integer, RangeValue>> chance) {
        for (Map.Entry<Enchantment, Map<Integer, RangeValue>> chanceMap : chance.entrySet()) {
            Map<Integer, RangeValue> levelMap = chanceMap.getValue();

            for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                levelEntry.setValue(levelEntry.getValue().multiply(condition.probability));
            }
        }
    }

    public static void applyRandomChanceWithLooting(IServerUtils ignoredUtils, LootItemRandomChanceWithLootingCondition condition, Map<Enchantment, Map<Integer, RangeValue>> chance) {
        Enchantment enchantment = Enchantments.MOB_LOOTING;
        Map<Integer, RangeValue> defaultMap = chance.get(null);
        Map<Integer, RangeValue> levelMap = chance.computeIfAbsent(enchantment, (k) -> new LinkedHashMap<>());
        RangeValue baseChance = defaultMap.get(0);

        defaultMap.put(0, baseChance.multiply(condition.percent));

        if (!levelMap.isEmpty()) {
            chance.get(null).get(0).multiply(condition.percent);

            for (Map.Entry<Integer, RangeValue> entry : levelMap.entrySet()) {
                int level = entry.getKey();
                float multiplier = condition.percent + (level * condition.lootingMultiplier);

                entry.setValue(entry.getValue().multiply(multiplier));
            }
        } else {
            for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                float multiplier = condition.percent + (level * condition.lootingMultiplier);

                levelMap.put(level, baseChance.multiply(multiplier));
            }
        }
    }

    public static void applyTableBonus(IServerUtils ignoredUtils, BonusLevelTableCondition condition, Map<Enchantment, Map<Integer, RangeValue>> chance) {
        if (condition.values.length == 0) {
            return;
        }

        Enchantment enchantment = condition.enchantment;
        Map<Integer, RangeValue> defaultMap = chance.get(null);
        RangeValue baseChance = defaultMap.get(0);
        Map<Integer, RangeValue> levelMap = chance.computeIfAbsent(enchantment, (k) -> new LinkedHashMap<>());

        defaultMap.put(0, baseChance.multiply(condition.values[0]));

        if (!levelMap.isEmpty()) {
            for (Map.Entry<Integer, RangeValue> entry : levelMap.entrySet()) {
                Integer level = entry.getKey();

                if (level < condition.values.length) {
                    entry.getValue().multiply(condition.values[level]);
                }
            }
        } else {
            int maxLevel = enchantment.getMaxLevel();

            for (int level = 1; level <= maxLevel; level++) {
                if (level < condition.values.length) {
                    levelMap.put(level, baseChance.multiply(condition.values[level]));
                }
            }
        }
    }

    public static void applySetCount(IServerUtils utils, SetItemCountFunction function, Map<Enchantment, Map<Integer, RangeValue>> count) {
        if (function.predicates.length == 0) {
            RangeValue modifierValue = utils.convertNumber(utils, function.value);

            for (Map.Entry<Enchantment, Map<Integer, RangeValue>> chanceMap : count.entrySet()) {
                Map<Integer, RangeValue> levelMap = chanceMap.getValue();

                for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                    if (function.add) {
                        levelEntry.setValue(levelEntry.getValue().add(modifierValue));
                    } else {
                        levelEntry.setValue(new RangeValue(modifierValue));
                    }
                }
            }
        }
    }

    public static void applyBonus(IServerUtils ignoredUtils, ApplyBonusCount function, Map<Enchantment, Map<Integer, RangeValue>> count) {
        if (function.predicates.length != 0) {
            return;
        }

        Enchantment enchantment = function.enchantment;
        Map<Integer, RangeValue> defaultMap = count.get(null);
        RangeValue baseCount = defaultMap.get(0);
        Map<Integer, RangeValue> levelMap = count.computeIfAbsent(enchantment, (k) -> new LinkedHashMap<>());

        defaultMap.put(0, calculateCount(function, baseCount, 0));

        if (!levelMap.isEmpty()) {
            for (Map.Entry<Integer, RangeValue> entry : levelMap.entrySet()) {
                entry.setValue(calculateCount(function, entry.getValue(), entry.getKey()));
            }
        } else {
            for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                levelMap.put(level, calculateCount(function, baseCount, level));
            }
        }
    }

    public static void applyLimitCount(IServerUtils utils, LimitCount function, Map<Enchantment, Map<Integer, RangeValue>> bonusCount) {
        if (function.predicates.length != 0) {
            return;
        }

        RangeValue limitMin = utils.convertNumber(utils, function.limiter.min);
        RangeValue limitMax = utils.convertNumber(utils, function.limiter.max);

        for (Map.Entry<Enchantment, Map<Integer, RangeValue>> entry : bonusCount.entrySet()) {
            Map<Integer, RangeValue> levelMap = entry.getValue();

            for (Map.Entry<Integer, RangeValue> mapEntry : levelMap.entrySet()) {
                mapEntry.setValue(mapEntry.getValue().clamp(limitMin, limitMax));
            }
        }
    }

    public static void applyLootingEnchant(IServerUtils utils, LootingEnchantFunction function, Map<Enchantment, Map<Integer, RangeValue>> count) {
        if (function.predicates.length != 0) {
            return;
        }

        Enchantment enchantment = Enchantments.MOB_LOOTING;
        RangeValue modifierBonus = utils.convertNumber(utils, function.value);
        RangeValue floorLimit = new RangeValue(false, true);
        RangeValue ceilLimit = function.limit > 0 ? new RangeValue(function.limit) : null;
        Map<Integer, RangeValue> levelMap = count.computeIfAbsent(enchantment, (k) -> new LinkedHashMap<>());

        if (!levelMap.isEmpty()) {
            for (Map.Entry<Integer, RangeValue> entry : levelMap.entrySet()) {
                int level = entry.getKey();
                RangeValue updatedValue = entry.getValue().add(modifierBonus.multiply(level));

                if (ceilLimit != null) {
                    updatedValue = updatedValue.clamp(floorLimit, ceilLimit);
                }

                entry.setValue(updatedValue);
            }
        } else {
            Map<Integer, RangeValue> defaultMap = count.get(null);
            RangeValue baseCount = defaultMap.get(0);
            int maxLevel = enchantment.getMaxLevel();

            for (int level = 1; level <= maxLevel; level++) {
                RangeValue updatedValue = baseCount.add(modifierBonus.multiply(level));

                if (ceilLimit != null) {
                    updatedValue = updatedValue.clamp(floorLimit, ceilLimit);
                }

                levelMap.put(level, updatedValue);
            }
        }
    }

    @NotNull
    public static ItemStack applyEnchantRandomlyItemStackModifier(IServerUtils ignoredUtils, EnchantRandomlyFunction function, ItemStack itemStack) {
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
    public static ItemStack applyEnchantWithLevelsItemStackModifier(IServerUtils ignoredUtils, EnchantWithLevelsFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.length == 0) {
            if (itemStack.is(Items.BOOK)) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            }
        }

        return itemStack;
    }

    public static ItemStack applySetAttributesItemStackModifier(IServerUtils ignoredUtils, SetAttributesFunction function, ItemStack itemStack) {
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

    public static ItemStack applySetNameItemStackModifier(IServerUtils ignoredUtils, SetNameFunction function, ItemStack itemStack) {
        if (function.predicates.length == 0) {
            itemStack.setHoverName(function.name);
        }

        return itemStack;
    }

    public static ItemStack applyItemStackModifier(IServerUtils ignoredUtils, LootItemFunction function, ItemStack itemStack) {
        if (function instanceof LootItemConditionalFunction conditional && conditional.predicates.length > 0) {
            return itemStack;
        }

        itemStack = function.apply(itemStack, null);
        return itemStack;
    }

    public static void addObjectFields(IServerUtils utils, TooltipBuilder tooltip, Object object, Class<?> baseClass) {
        if (object.getClass().isEnum()) {
            return;
        }

        List<Field> fields = TooltipUtils.getAllFields(object.getClass(), baseClass);
        List<Field> names = fields.stream().filter((f) -> !Modifier.isStatic(f.getModifiers())).toList();

        names.forEach((f) -> {
            if (f.isSynthetic()) {
                return;
            }

            try {
                f.setAccessible(true);

                Object obj = f.get(object);
                String key = f.getName();
                TooltipBuilder builder = utils.getValueTooltip(utils, obj);

                if (builder.hasKey()) {
                    tooltip.add(TooltipBuilder.array((b) -> b.add(builder).rawKey(key)));
                } else {
                    tooltip.add(builder.rawKey(key));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static TooltipBuilder getJsonTooltip(IServerUtils utils, JsonElement element) {
        if (element.isJsonObject()) {
            return TooltipBuilder.array((b) -> element.getAsJsonObject().asMap().forEach((key, e) -> b.add(getElementTooltip(utils, e).rawKey(key))));
        }

        return getElementTooltip(utils, element);
    }

    private static TooltipBuilder getElementTooltip(IServerUtils utils, JsonElement element) {
        if (element.isJsonObject()) {
            return TooltipBuilder.array((b) -> element.getAsJsonObject().asMap().forEach((key, e) -> b.add(getElementTooltip(utils, e)).rawKey(key)));
        } else if (element.isJsonArray()) {
            return TooltipBuilder.array((b) -> element.getAsJsonArray().forEach((e) -> b.add(getElementTooltip(utils, e))));
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();

            if (jsonPrimitive.isBoolean()) {
                return utils.getValueTooltip(utils, jsonPrimitive.getAsBoolean());
            } else if (jsonPrimitive.isString()) {
                return utils.getValueTooltip(utils, jsonPrimitive.getAsString());
            } else if (jsonPrimitive.isNumber()) {
                return utils.getValueTooltip(utils, jsonPrimitive.getAsNumber());
            }
        }

        return TooltipBuilder.empty();
    }

    private static RangeValue calculateCount(ApplyBonusCount function, RangeValue value, int level) {
        if (function.formula instanceof ApplyBonusCount.OreDrops) {
            if (level > 0) {
                return value.multiplyMax(level + 1);
            }
        } else if (function.formula instanceof ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCount) {
            return value.addMax(binomialWithBonusCount.extraRounds + level);
        } else if (function.formula instanceof ApplyBonusCount.UniformBonusCount uniformBonusCount) {
            if (level > 0) {
                return value.addMax(uniformBonusCount.bonusMultiplier * level);
            }
        }

        return value;
    }

    @NotNull
    private static List<Field> getAllFields(Class<?> clazz, Class<?> baseClass) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != baseClass && currentClass != Object.class) {
            Field[] declaredFields = currentClass.getDeclaredFields();

            Collections.addAll(fields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }
}
