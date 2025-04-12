package com.yanny.ali.manager;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class AliServerRegistry implements IServerRegistry, IServerUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<LootPoolEntryType, BiFunction<IServerUtils, LootPoolEntryContainer, List<Item>>> entryItemCollectorMap = new HashMap<>();
    private final Map<LootItemFunctionType<?>, TriFunction<IServerUtils, List<Item>, LootItemFunction, List<Item>>> functionItemCollectorMap = new HashMap<>();
    private final Map<LootItemConditionType, TriFunction<IServerUtils, List<Item>, LootItemCondition, List<Item>>> conditionItemCollectorMap = new HashMap<>();
    private final Map<ResourceKey<LootTable>, LootTable> lootTableMap = new HashMap<>();
    private ServerLevel serverLevel;

    public void addLootTable(ResourceKey<LootTable> resourceLocation, LootTable lootTable) {
        lootTableMap.put(resourceLocation, lootTable);
    }

    public void setServerLevel(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    @Override
    public <T extends LootPoolEntryContainer> void registerItemCollector(LootPoolEntryType type, BiFunction<IServerUtils, T, List<Item>> itemSupplier) {
        //noinspection unchecked
        entryItemCollectorMap.put(type, (u, e) -> itemSupplier.apply(u, (T) e));
    }

    @Override
    public <T extends LootItemCondition> void registerItemCollector(LootItemConditionType type, TriFunction<IServerUtils, List<Item>, T, List<Item>> itemSupplier) {
        //noinspection unchecked
        conditionItemCollectorMap.put(type, (u, l, f) -> itemSupplier.apply(u, l, (T) f));
    }

    @Override
    public <T extends LootItemFunction> void registerItemCollector(LootItemFunctionType<?> type, TriFunction<IServerUtils, List<Item>, T, List<Item>> itemSupplier) {
        //noinspection unchecked
        functionItemCollectorMap.put(type, (u, l, f) -> itemSupplier.apply(u, l, (T) f));
    }

    @Override
    public <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry) {
        BiFunction<IServerUtils, LootPoolEntryContainer, List<Item>> itemSupplier = entryItemCollectorMap.get(entry.getType());

        if (itemSupplier != null) {
            return itemSupplier.apply(utils, entry);
        } else {
            return List.of();
        }
    }

    @Override
    public <T extends LootItemCondition> List<Item> collectItems(IServerUtils utils, List<Item> items, T condition) {
        TriFunction<IServerUtils, List<Item>, LootItemCondition, List<Item>> itemSupplier = conditionItemCollectorMap.get(condition.getType());

        if (itemSupplier != null) {
            return itemSupplier.apply(utils, items, condition);
        } else {
            return List.of();
        }
    }

    @Override
    public <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function) {
        TriFunction<IServerUtils, List<Item>, LootItemFunction, List<Item>> itemSupplier = functionItemCollectorMap.get(function.getType());

        if (itemSupplier != null) {
            return itemSupplier.apply(utils, items, function);
        } else {
            return List.of();
        }
    }

    @Nullable
    @Override
    public ServerLevel getServerLevel() {
        return serverLevel;
    }

    @Nullable
    @Override
    public LootTable getLootTable(Either<ResourceKey<LootTable>, LootTable> either) {
        return either.map(lootTableMap::get, lootTable -> lootTable);
    }

    public void printServerInfo() {
        LOGGER.info("Registered {} entry item collectors", entryItemCollectorMap.size());
        LOGGER.info("Registered {} condition item collectors", conditionItemCollectorMap.size());
        LOGGER.info("Registered {} function item collectors", functionItemCollectorMap.size());
    }
}
