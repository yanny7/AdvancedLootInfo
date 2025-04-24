package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.RangeValue;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TooltipUtils {
    private TooltipUtils() {}

    @NotNull
    public static Map<Holder<Enchantment>, Map<Integer, RangeValue>> getChance(IClientUtils utils, List<LootItemCondition> conditions, float rawChance) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance = new LinkedHashMap<>();

        chance.put(null, Map.of(0, new RangeValue(rawChance * 100)));

        for (LootItemCondition condition : conditions) {
            utils.applyChanceModifier(utils, condition, chance);
        }

        return chance;
    }

    @NotNull
    public static Map<Holder<Enchantment>, Map<Integer, RangeValue>> getCount(IClientUtils utils, List<LootItemFunction> functions) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> count = new LinkedHashMap<>();

        count.put(null, Map.of(0, new RangeValue()));

        for (LootItemFunction function : functions) {
            utils.applyCountModifier(utils, function, count);
        }

        return count;
    }

    public static ItemStack getItemStack(IClientUtils utils, LootPoolEntryContainer entry, Item item) {
        ItemStack itemStack = item.getDefaultInstance();

        if (entry.conditions.isEmpty() && entry instanceof LootPoolSingletonContainer container) {
            for (LootItemFunction function : container.functions) {
                itemStack = utils.applyItemStackModifier(utils, function, itemStack);
            }
        }

        return itemStack;
    }

    public static void applyRandomChance(IClientUtils utils, LootItemRandomChanceCondition condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> chanceMap : chance.entrySet()) {
            for (Map.Entry<Integer, RangeValue> levelEntry : chanceMap.getValue().entrySet()) {
                levelEntry.getValue().multiply(condition.probability());
            }
        }
    }

    public static void applyRandomChanceWithLooting(IClientUtils utils, LootItemRandomChanceWithLootingCondition condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        Holder<Enchantment> enchantment = Holder.direct(Enchantments.LOOTING);

        if (chance.containsKey(enchantment)) {
            chance.get(null).get(0).multiply(condition.percent());

            for (Map.Entry<Integer, RangeValue> entry : chance.get(enchantment).entrySet()) {
                entry.getValue().multiply(condition.percent() + entry.getKey() * condition.lootingMultiplier());
            }
        } else {
            RangeValue baseChance = new RangeValue(chance.get(null).get(0));
            Map<Integer, RangeValue> levelMap = new LinkedHashMap<>();

            chance.get(null).get(0).multiply(condition.percent());
            chance.put(enchantment, levelMap);

            for (int level = 1; level <= enchantment.value().getMaxLevel(); level++) {
                levelMap.put(level, new RangeValue(baseChance).multiply(condition.percent() + level * condition.lootingMultiplier()));
            }
        }
    }

    public static void applyTableBonus(IClientUtils utils, BonusLevelTableCondition condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
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

    public static void applySetCount(IClientUtils utils, SetItemCountFunction function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
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

    public static void applyBonus(IClientUtils utils, ApplyBonusCount function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
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

    public static void applyLimitCount(IClientUtils utils, LimitCount function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> bonusCount) {
        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> entry : bonusCount.entrySet()) {
            for (Map.Entry<Integer, RangeValue> mapEntry : entry.getValue().entrySet()) {
                RangeValue value = mapEntry.getValue();
                value.clamp(utils.convertNumber(utils, function.limiter.min), utils.convertNumber(utils, function.limiter.max));
            }
        }
    }

    public static void applyLootingEnchant(IClientUtils utils, LootingEnchantFunction function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        Holder<Enchantment> enchantment = Holder.direct(Enchantments.LOOTING);

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

    @NotNull
    public static ItemStack applyEnchantRandomlyItemStackModifier(IClientUtils utils, EnchantRandomlyFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.isEmpty()) {
            boolean isBook = itemStack.is(Items.BOOK);
            ItemStack finalItemStack = itemStack;
            Optional<HolderSet<Enchantment>> enchantments = function.enchantments;

            if (enchantments.isEmpty()) {
                List<Holder.Reference<Enchantment>> list = BuiltInRegistries.ENCHANTMENT.holders().filter((ref) -> ref.value().isDiscoverable()).filter((ref) -> isBook || ref.value().canEnchant(finalItemStack)).toList();

                if (list.size() == 1) {
                    enchantments = Optional.of(HolderSet.direct(list.getFirst()));
                }
            }

            if (enchantments.isPresent() && enchantments.get().size() == 1 && enchantments.get().get(0).value().getMinLevel() == enchantments.get().get(0).value().getMaxLevel()) {
                itemStack.enchant(enchantments.get().get(0).value(), enchantments.get().get(0).value().getMaxLevel());
            } else if (isBook) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            }
        }

        return itemStack;
    }

    @NotNull
    public static ItemStack applyEnchantWithLevelsItemStackModifier(IClientUtils utils, EnchantWithLevelsFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.isEmpty()) {
            if (itemStack.is(Items.BOOK)) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            }
        }

        return itemStack;
    }

    public static ItemStack applySetAttributesItemStackModifier(IClientUtils utils, SetAttributesFunction function, ItemStack itemStack) {
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

    public static ItemStack applySetNameItemStackModifier(IClientUtils utils, SetNameFunction function, ItemStack itemStack) {
        if (function.predicates.isEmpty() && function.name.isPresent()) {
            itemStack.set(function.target.component(), function.name.get());
        }

        return itemStack;
    }

    public static ItemStack applyItemStackModifier(IClientUtils utils, LootItemFunction function, ItemStack itemStack) {
        if (function instanceof LootItemConditionalFunction conditional && !conditional.predicates.isEmpty()) {
            return itemStack;
        }

        itemStack = function.apply(itemStack, null);
        return itemStack;
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

    private static ItemAttributeModifiers updateModifiers(List<SetAttributesFunction.Modifier> modifiers, ItemAttributeModifiers itemAttributeModifiers) {
        for (SetAttributesFunction.Modifier modifier : modifiers) {
            UUID id = modifier.id().orElse(UUID.randomUUID());

            if (modifier.slots().size() == 1 && modifier.amount().getType() == NumberProviders.CONSTANT) {
                EquipmentSlotGroup equipmentSlot = Util.getRandom(modifier.slots(), RandomSource.create());
                ConstantValue value = (ConstantValue) modifier.amount();

                itemAttributeModifiers = itemAttributeModifiers.withModifierAdded(modifier.attribute(), new AttributeModifier(id, modifier.name(), value.getFloat(null), modifier.operation()), equipmentSlot);
            }
        }

        return itemAttributeModifiers;
    }
}
