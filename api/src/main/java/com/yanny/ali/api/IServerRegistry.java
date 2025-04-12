package com.yanny.ali.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.apache.commons.lang3.function.TriFunction;

import java.util.List;
import java.util.function.BiFunction;

public interface IServerRegistry {
    <T extends LootPoolEntryContainer> void registerItemCollector(LootPoolEntryType type, BiFunction<IServerUtils, T, List<Item>> itemSupplier);
    <T extends LootItemCondition> void registerItemCollector(LootItemConditionType type, TriFunction<IServerUtils, List<Item>, T, List<Item>> itemSupplier);
    <T extends LootItemFunction> void registerItemCollector(LootItemFunctionType<?> type, TriFunction<IServerUtils, List<Item>, T, List<Item>> itemSupplier);
}
