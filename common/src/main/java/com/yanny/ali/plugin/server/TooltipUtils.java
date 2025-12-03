package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ErrorTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ValueTooltipNode;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
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
            for (Map.Entry<Integer, RangeValue> levelEntry : chanceMap.getValue().entrySet()) {
                levelEntry.getValue().multiply(utils.convertNumber(utils, condition.chance()));
            }
        }
    }

    public static void applyRandomChanceWithLooting(IServerUtils utils, LootItemRandomChanceWithEnchantedBonusCondition condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
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

    public static void applyTableBonus(IServerUtils utils, BonusLevelTableCondition condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
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

    public static void applySetCount(IServerUtils utils, SetItemCountFunction function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        if (function.predicates.isEmpty()) {
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
    }

    public static void applyBonus(IServerUtils utils, ApplyBonusCount function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        if (function.predicates.isEmpty()) {
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
    }

    public static void applyLimitCount(IServerUtils utils, LimitCount function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> bonusCount) {
        if (function.predicates.isEmpty()) {
            for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> entry : bonusCount.entrySet()) {
                for (Map.Entry<Integer, RangeValue> mapEntry : entry.getValue().entrySet()) {
                    RangeValue value = mapEntry.getValue();
                    value.clamp(utils.convertNumber(utils, function.limiter.min), utils.convertNumber(utils, function.limiter.max));
                }
            }
        }
    }

    public static void applyLootingEnchant(IServerUtils utils, EnchantedCountIncreaseFunction function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        if (function.predicates.isEmpty()) {
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
    public static ItemStack applyEnchantWithLevelsItemStackModifier(IServerUtils utils, EnchantWithLevelsFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.isEmpty()) {
            if (itemStack.is(Items.BOOK)) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            } else {
                itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        return itemStack;
    }

    public static ItemStack applySetAttributesItemStackModifier(IServerUtils utils, SetAttributesFunction function, ItemStack itemStack) {
        if (function.predicates.isEmpty()) {
            if (function.replace) {
                itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, updateModifiers(function.modifiers, ItemAttributeModifiers.EMPTY));
            } else {
                itemStack.update(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, (modifiers) -> updateModifiers(function.modifiers, modifiers));
            }
        }

        return itemStack;
    }

    public static ItemStack applySetNameItemStackModifier(IServerUtils utils, SetNameFunction function, ItemStack itemStack) {
        if (function.predicates.isEmpty() && function.name.isPresent()) {
            itemStack.set(function.target.component(), function.name.get());
        }

        return itemStack;
    }

    @NotNull
    public static ItemStack applySetEnchantmentsItemStackModifier(IServerUtils utils, SetEnchantmentsFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.isEmpty()) {
            if (itemStack.is(Items.BOOK)) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            } else {
                itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        return itemStack;
    }

    public static ItemStack applyItemStackModifier(IServerUtils utils, LootItemFunction function, ItemStack itemStack) {
        if (function instanceof LootItemConditionalFunction conditional && !conditional.predicates.isEmpty()) {
            return itemStack;
        }

        itemStack = function.apply(itemStack, null);
        return itemStack;
    }

    public static void addObjectFields(IServerUtils utils, IKeyTooltipNode tooltip, Object object, Class<?> baseClass) {
        List<Field> fields = TooltipUtils.getAllFields(object.getClass(), baseClass);
        List<Field> names = fields.stream().filter((f) -> !Modifier.isStatic(f.getModifiers())).toList();

        names.forEach((f) -> {
            f.setAccessible(true);

            try {
                Object obj = f.get(object);
                IKeyTooltipNode t;

                if (obj instanceof LootItemCondition condition) {
                    t = BranchTooltipNode.branch().add(GenericTooltipUtils.getConditionListTooltip(utils, Collections.singletonList(condition)));
                } else if (obj instanceof LootItemFunction function) {
                    t = BranchTooltipNode.branch().add(GenericTooltipUtils.getFunctionListTooltip(utils, Collections.singletonList(function)));
                } else {
                    t = utils.getValueTooltip(utils, obj);
                }

                if (t instanceof ValueTooltipNode.Builder builder) {
                    tooltip.add(builder.build(f.getName(), false));
                } else if (t instanceof BranchTooltipNode.Builder builder) {
                    tooltip.add(builder.build(f.getName() + ":", false));
                } else if (t instanceof ErrorTooltipNode.Builder) {
                    tooltip.add(ValueTooltipNode.keyValue(f.getName(), "[" + f.getType().getName() + "]").build("ali.property.value.null"));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static IKeyTooltipNode getArrayTooltip(IServerUtils utils, Object value) {
        Class<?> componentType = value.getClass().getComponentType();
        IKeyTooltipNode tooltip = BranchTooltipNode.branch();
        int length = Array.getLength(value);

        if (componentType == LootItemCondition.class) {
            List<LootItemCondition> values = new ArrayList<>();

            for (int i = 0; i < length; i++) {
                values.add((LootItemCondition) Array.get(value, i));
            }

            return GenericTooltipUtils.getSubConditionsTooltip(utils, values);
        } else {
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(value, i);

                    if (element instanceof LootItemCondition condition) {
                        tooltip.add(GenericTooltipUtils.getConditionListTooltip(utils, Collections.singletonList(condition)));
                    } else if (element instanceof LootItemFunction function) {
                        tooltip.add(GenericTooltipUtils.getFunctionListTooltip(utils, Collections.singletonList(function)));
                    } else {
                        IKeyTooltipNode t = utils.getValueTooltip(utils, element);

                        if (t instanceof ValueTooltipNode.Builder builder) {
                            tooltip.add(builder.build("ali.property.value.null"));
                        } else if (t instanceof BranchTooltipNode.Builder builder) {
                            tooltip.add(builder.build("ali.property.branch.values"));
                        } else if (t instanceof ErrorTooltipNode.Builder) {
                            tooltip.add(utils.getValueTooltip(utils, element).build("ali.property.value.null"));
                        }
                    }
                }
            } else {
                return ValueTooltipNode.value("[]");
            }
        }

        return tooltip;
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
