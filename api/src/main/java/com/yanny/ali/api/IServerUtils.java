package com.yanny.ali.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface IServerUtils extends ICommonUtils {
    <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry);

    <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function);

    <T extends LootPoolEntryContainer> EntryFactory<T> getEntryFactory(IServerUtils utils, T type);

    <T extends LootItemFunction> ITooltipNode getFunctionTooltip(IServerUtils utils, T function);

    <T extends LootItemCondition> ITooltipNode getConditionTooltip(IServerUtils utils, T condition);

    <T extends LootItemFunction> void applyCountModifier(IServerUtils utils, T function, Map<Enchantment, Map<Integer, RangeValue>> count);

    <T extends LootItemCondition> void applyChanceModifier(IServerUtils utils, T condition, Map<Enchantment, Map<Integer, RangeValue>> chance);

    <T extends LootItemFunction> ItemStack applyItemStackModifier(IServerUtils utils, T function, ItemStack itemStack);

    RangeValue convertNumber(IServerUtils utils, @Nullable NumberProvider numberProvider);

    @Nullable
    ServerLevel getServerLevel();

    LootContext getLootContext();

    LootTable getLootTable(ResourceLocation location);

    List<LootPool> getLootPools(LootTable lootTable);

    @FunctionalInterface
    interface EntryFactory<T extends LootPoolEntryContainer> {
        IDataNode create(List<ILootModifier<?>> modifiers, IServerUtils utils, T entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions);
    }
}
