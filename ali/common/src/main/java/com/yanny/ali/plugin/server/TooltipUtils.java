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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TooltipUtils {
    public static ItemStack getItemStack(IServerUtils utils, ItemStack itemStack, List<LootItemFunction> functions) {
        for (LootItemFunction function : functions) {
            itemStack = utils.applyItemStackModifier(utils, function, itemStack);
        }

        return itemStack;
    }

    public static void applyRandomChance(IServerUtils ignoredUtils, LootItemRandomChanceCondition condition, EnchantedRanges chance) {
        chance.modifyAllEntries((range) -> range.multiply(condition.probability));
    }

    public static void applyRandomChanceWithLooting(IServerUtils ignoredUtils, LootItemRandomChanceWithLootingCondition condition, EnchantedRanges chance) {
        chance.computeAllLevels(Enchantments.MOB_LOOTING, (level, value) -> {
            float multiplier = condition.percent + (level * condition.lootingMultiplier);
            return value.multiply(multiplier);
        });
    }

    public static void applyTableBonus(IServerUtils ignoredUtils, BonusLevelTableCondition condition, EnchantedRanges chance) {
        if (condition.values.length == 0) {
            return;
        }

        chance.computeAllLevels(condition.enchantment, (level, value) -> {
            if (level < condition.values.length) {
                return value.multiply(condition.values[level]);
            }

            return value;
        });
    }

    public static void applySetCount(IServerUtils utils, SetItemCountFunction function, EnchantedRanges count) {
        if (function.predicates.length != 0) {
            return;
        }

        RangeValue modifierValue = utils.convertNumber(utils, function.value);

        count.modifyAllEntries((value) -> {
            if (function.add) {
                return value.add(modifierValue);
            } else {
                return modifierValue;
            }
        });
    }

    public static void applyBonus(IServerUtils ignoredUtils, ApplyBonusCount function, EnchantedRanges count) {
        if (function.predicates.length != 0) {
            return;
        }

        count.computeAllLevels(function.enchantment, (level, value) -> calculateCount(function, value, level));
    }

    public static void applyLimitCount(IServerUtils utils, LimitCount function, EnchantedRanges bonusCount) {
        if (function.predicates.length != 0) {
            return;
        }

        RangeValue limitMin = utils.convertNumber(utils, function.limiter.min);
        RangeValue limitMax = utils.convertNumber(utils, function.limiter.max);

        bonusCount.modifyAllEntries((value) -> value.clamp(limitMin, limitMax));
    }

    public static void applyLootingEnchant(IServerUtils utils, LootingEnchantFunction function, EnchantedRanges count) {
        if (function.predicates.length != 0) {
            return;
        }

        RangeValue modifierBonus = utils.convertNumber(utils, function.value);
        RangeValue floorLimit = new RangeValue(false, true);
        RangeValue ceilLimit = function.limit > 0 ? new RangeValue(function.limit) : null;

        count.computeLevels(Enchantments.MOB_LOOTING, (level, value) -> {
            RangeValue updatedValue = value.add(modifierBonus.multiply(level));

            if (ceilLimit != null) {
                updatedValue = updatedValue.clamp(floorLimit, ceilLimit);
            }

            return updatedValue;
        });
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
