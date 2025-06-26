package com.yanny.ali.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public interface IServerRegistry {
    <T extends LootPoolEntryContainer> void registerItemCollector(LootPoolEntryType type, BiFunction<IServerUtils, T, List<Item>> itemSupplier);

    <T extends LootItemFunction> void registerItemCollector(LootItemFunctionType type, TriFunction<IServerUtils, List<Item>, T, List<Item>> itemSupplier);

    <T extends LootPoolEntryContainer> void registerEntry(LootPoolEntryType type, IServerUtils.EntryFactory<T> entryFactory);

    <T extends LootItemFunction> void registerFunctionTooltip(LootItemFunctionType type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends LootItemCondition> void registerConditionTooltip(LootItemConditionType type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends NumberProvider> void registerNumberProvider(LootNumberProviderType type, BiFunction<IServerUtils, T, RangeValue> converter);

    <T extends LootItemFunction> void registerCountModifier(LootItemFunctionType type, TriConsumer<IServerUtils, T, Map<Enchantment, Map<Integer, RangeValue>>> consumer);

    <T extends LootItemCondition> void registerChanceModifier(LootItemConditionType type, TriConsumer<IServerUtils, T, Map<Enchantment, Map<Integer, RangeValue>>> consumer);

    <T extends LootItemFunction> void registerItemStackModifier(LootItemFunctionType type, TriFunction<IServerUtils, T, ItemStack, ItemStack> consumer);

}
