package com.yanny.ali.plugin.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

public class TooltipUtils {
    public static ItemStack getItemStack(IServerUtils utils, ItemStack itemStack, List<LootItemFunction> functions) {
        for (LootItemFunction function : functions) {
            itemStack = utils.applyItemStackModifier(utils, function, itemStack);
        }

        return itemStack;
    }

    public static void applyRandomChance(IServerUtils utils, LootItemRandomChanceCondition condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> chanceMap : chance.entrySet()) {
            Map<Integer, RangeValue> levelMap = chanceMap.getValue();

            for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                levelEntry.setValue(levelEntry.getValue().multiply(utils.convertNumber(utils, condition.chance())));
            }
        }
    }

    public static void applyRandomChanceWithLooting(IServerUtils ignoredUtils, LootItemRandomChanceWithEnchantedBonusCondition condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        Holder<Enchantment> enchantment = condition.enchantment();
        Map<Integer, RangeValue> defaultMap = chance.get(null);
        Map<Integer, RangeValue> levelMap = chance.computeIfAbsent(enchantment, (k) -> new LinkedHashMap<>());
        RangeValue baseChance = defaultMap.get(0);

        defaultMap.put(0, baseChance.multiply(condition.unenchantedChance()));

        if (!levelMap.isEmpty()) {
            for (Map.Entry<Integer, RangeValue> entry : levelMap.entrySet()) {
                int level = entry.getKey();
                RangeValue multiplier = new RangeValue(1);

                entry.setValue(entry.getValue().multiply(calculateCount(condition.enchantedChance(), multiplier, level)));
            }
        } else {
            for (int level = 1; level <= enchantment.value().getMaxLevel(); level++) {
                RangeValue multiplier = new RangeValue(1);

                levelMap.put(level, baseChance.multiply(calculateCount(condition.enchantedChance(), multiplier, level)));
            }
        }
    }

    public static void applyTableBonus(IServerUtils ignoredUtils, BonusLevelTableCondition condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        if (condition.values().isEmpty()) {
            return;
        }

        Holder<Enchantment> enchantment = condition.enchantment();
        Map<Integer, RangeValue> defaultMap = chance.get(null);
        RangeValue baseChance = defaultMap.get(0);
        Map<Integer, RangeValue> levelMap = chance.computeIfAbsent(enchantment, (k) -> new LinkedHashMap<>());

        defaultMap.put(0, baseChance.multiply(condition.values().getFirst()));

        if (!levelMap.isEmpty()) {
            for (Map.Entry<Integer, RangeValue> entry : levelMap.entrySet()) {
                Integer level = entry.getKey();

                if (level < condition.values().size()) {
                    entry.getValue().multiply(condition.values().get(level));
                }
            }
        } else {
            int maxLevel = enchantment.value().getMaxLevel();

            for (int level = 1; level <= maxLevel; level++) {
                if (level < condition.values().size()) {
                    levelMap.put(level, baseChance.multiply(condition.values().get(level)));
                }
            }
        }
    }

    public static void applySetCount(IServerUtils utils, SetItemCountFunction function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        if (function.predicates.isEmpty()) {
            RangeValue modifierValue = utils.convertNumber(utils, function.value);

            for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> chanceMap : count.entrySet()) {
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

    public static void applyBonus(IServerUtils ignoredUtils, ApplyBonusCount function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        if (!function.predicates.isEmpty()) {
            return;
        }

        Holder<Enchantment> enchantment = function.enchantment;
        Map<Integer, RangeValue> defaultMap = count.get(null);
        RangeValue baseCount = defaultMap.get(0);
        Map<Integer, RangeValue> levelMap = count.computeIfAbsent(enchantment, (k) -> new LinkedHashMap<>());

        defaultMap.put(0, calculateCount(function, baseCount, 0));

        if (!levelMap.isEmpty()) {
            for (Map.Entry<Integer, RangeValue> entry : levelMap.entrySet()) {
                entry.setValue(calculateCount(function, entry.getValue(), entry.getKey()));
            }
        } else {
            for (int level = 1; level <= enchantment.value().getMaxLevel(); level++) {
                levelMap.put(level, calculateCount(function, baseCount, level));
            }
        }
    }

    public static void applyLimitCount(IServerUtils utils, LimitCount function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> bonusCount) {
        if (!function.predicates.isEmpty()) {
            return;
        }

        RangeValue limitMin = utils.convertNumber(utils, function.limiter.min);
        RangeValue limitMax = utils.convertNumber(utils, function.limiter.max);

        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> entry : bonusCount.entrySet()) {
            Map<Integer, RangeValue> levelMap = entry.getValue();

            for (Map.Entry<Integer, RangeValue> mapEntry : levelMap.entrySet()) {
                mapEntry.setValue(mapEntry.getValue().clamp(limitMin, limitMax));
            }
        }
    }

    public static void applyLootingEnchant(IServerUtils utils, EnchantedCountIncreaseFunction function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        if (!function.predicates.isEmpty()) {
            return;
        }

        Holder<Enchantment> enchantment = function.enchantment;
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
            int maxLevel = enchantment.value().getMaxLevel();

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
    public static ItemStack applyEnchantRandomlyItemStackModifier(IServerUtils utils, EnchantRandomlyFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.isEmpty()) {
            boolean isBook = itemStack.is(Items.BOOK);
            boolean compatible = !isBook && function.onlyCompatible;
            ItemStack finalItemStack = itemStack;
            Optional<HolderSet<Enchantment>> enchantments = function.options;

            if (enchantments.isEmpty()) {
                List<Holder<Enchantment>> list = function.options.map(HolderSet::stream).orElseGet(() -> utils.lookupProvider().lookupOrThrow(Registries.ENCHANTMENT)
                        .listElements().map(Function.identity())).filter((ref) -> compatible || ref.value().canEnchant(finalItemStack)).toList();

                if (list.size() == 1) {
                    enchantments = Optional.of(HolderSet.direct(list.getFirst()));
                }
            }

            if (enchantments.isPresent() && enchantments.get().size() == 1 && enchantments.get().get(0).value().getMinLevel() == enchantments.get().get(0).value().getMaxLevel()) {
                itemStack.enchant(enchantments.get().get(0), enchantments.get().get(0).value().getMaxLevel());
            } else if (isBook) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            } else {
                itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        return itemStack;
    }

    @NotNull
    public static ItemStack applyEnchantWithLevelsItemStackModifier(IServerUtils ignoredUtils, EnchantWithLevelsFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.isEmpty()) {
            if (itemStack.is(Items.BOOK)) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            } else {
                itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        return itemStack;
    }

    public static ItemStack applySetAttributesItemStackModifier(IServerUtils ignoredUtils, SetAttributesFunction function, ItemStack itemStack) {
        if (function.predicates.isEmpty()) {
            if (function.replace) {
                itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, updateModifiers(function.modifiers, ItemAttributeModifiers.EMPTY));
            } else {
                itemStack.update(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, (modifiers) -> {
                    if (modifiers.modifiers().isEmpty()) {
                        return updateModifiers(function.modifiers, itemStack.getItem().getDefaultAttributeModifiers());
                    } else {
                        return updateModifiers(function.modifiers, modifiers);
                    }
                });
            }
        }

        return itemStack;
    }

    public static ItemStack applySetNameItemStackModifier(IServerUtils ignoredUtils, SetNameFunction function, ItemStack itemStack) {
        if (function.predicates.isEmpty() && function.name.isPresent()) {
            itemStack.set(function.target.component(), function.name.get());
        }

        return itemStack;
    }

    @NotNull
    public static ItemStack applySetEnchantmentsItemStackModifier(IServerUtils ignoredUtils, SetEnchantmentsFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.isEmpty()) {
            if (itemStack.is(Items.BOOK)) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            } else {
                itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        return itemStack;
    }

    public static ItemStack applyItemStackModifier(IServerUtils ignoredUtils, LootItemFunction function, ItemStack itemStack) {
        if (function instanceof LootItemConditionalFunction conditional && !conditional.predicates.isEmpty()) {
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
        switch (function.formula) {
            case ApplyBonusCount.OreDrops ignored -> {
                if (level > 0) {
                    return value.multiplyMax(level + 1);
                }
            }
            case ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCount -> {
                return value.addMax(binomialWithBonusCount.extraRounds() + level);
            }
            case ApplyBonusCount.UniformBonusCount(int bonusMultiplier) -> {
                if (level > 0) {
                    return value.addMax(bonusMultiplier * level);
                }
            }
            default -> {
                return value;
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

    public static RangeValue calculateCount(LevelBasedValue levelBasedValue, RangeValue v, int level) {
        return switch (levelBasedValue) {
            case LevelBasedValue.Constant(float value) -> v.multiply(value);
            case LevelBasedValue.Clamped(LevelBasedValue value, float min, float max) -> {
                RangeValue processedInner = calculateCount(value, v, level);
                yield processedInner.clamp(min, max);
            }
            case LevelBasedValue.Fraction(LevelBasedValue numerator, LevelBasedValue denominator) -> {
                RangeValue initial = new RangeValue(1);
                RangeValue n = calculateCount(numerator, initial, level);
                RangeValue d = calculateCount(denominator, initial, level);
                RangeValue fractionResult = new RangeValue(n.min() / d.max(), n.max() / d.min());
                yield v.multiply(fractionResult);
            }
            case LevelBasedValue.Linear(float base, float perLevelAboveFirst) -> {
                RangeValue linearModifier = new RangeValue(base).add(perLevelAboveFirst * (level - 1));
                yield v.multiply(linearModifier);
            }
            case LevelBasedValue.LevelsSquared(float added) -> v.multiply(added + Mth.square(level));
            case LevelBasedValue.Lookup(List<Float> values, LevelBasedValue fallback) -> {
                if (level <= values.size()) {
                    yield v.multiply(values.get(level - 1));
                } else {
                    yield calculateCount(fallback, v, level);
                }
            }
            default -> new RangeValue(false, true);
        };
    }

    private static ItemAttributeModifiers updateModifiers(List<SetAttributesFunction.Modifier> modifiers, ItemAttributeModifiers itemAttributeModifiers) {
        for (SetAttributesFunction.Modifier modifier : modifiers) {
            ResourceLocation id = modifier.id();

            if (modifier.slots().size() == 1 && modifier.amount().getType() == NumberProviders.CONSTANT) {
                EquipmentSlotGroup equipmentSlot = Util.getRandom(modifier.slots(), RandomSource.create());
                ConstantValue value = (ConstantValue) modifier.amount();

                itemAttributeModifiers = itemAttributeModifiers.withModifierAdded(modifier.attribute(), new AttributeModifier(id, value.getFloat(null), modifier.operation()), equipmentSlot);
            }
        }

        return itemAttributeModifiers;
    }
}
