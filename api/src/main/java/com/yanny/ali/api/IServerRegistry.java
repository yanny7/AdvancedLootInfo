package com.yanny.ali.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface IServerRegistry {
    <T extends LootPoolEntryContainer> void registerItemCollector(Class<?> type, BiFunction<IServerUtils, T, List<Item>> itemSupplier);

    <T extends LootItemFunction> void registerItemCollector(Class<T> type, TriFunction<IServerUtils, List<Item>, T, List<Item>> itemSupplier);

    <T extends LootPoolEntryContainer> void registerEntry(Class<T> type, IServerUtils.EntryFactory<T> entryFactory);

    <T extends LootItemFunction> void registerFunctionTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends LootItemCondition> void registerConditionTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends Ingredient> void registerIngredientTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends NumberProvider> void registerNumberProvider(Class<T> type, BiFunction<IServerUtils, T, RangeValue> converter);

    <T extends LootItemFunction> void registerCountModifier(Class<T> type, TriConsumer<IServerUtils, T, Map<Enchantment, Map<Integer, RangeValue>>> consumer);

    <T extends LootItemCondition> void registerChanceModifier(Class<T> type, TriConsumer<IServerUtils, T, Map<Enchantment, Map<Integer, RangeValue>>> consumer);

    <T extends LootItemFunction> void registerItemStackModifier(Class<T> type, TriFunction<IServerUtils, T, ItemStack, ItemStack> consumer);

    void registerLootModifiers(Function<IServerUtils, List<ILootModifier<?>>> getter);
}
