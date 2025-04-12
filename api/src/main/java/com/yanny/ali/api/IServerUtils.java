package com.yanny.ali.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IServerUtils extends ICommonUtils {
    <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry);
    <T extends LootItemCondition> List<Item> collectItems(IServerUtils utils, List<Item> items, T condition);
    <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function);

    @Nullable
    ServerLevel getServerLevel();
}
