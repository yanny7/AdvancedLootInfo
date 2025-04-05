package com.yanny.ali.plugin;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class Utils implements IWidgetUtils {
    @Override
    public Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<LootPoolEntryContainer> entries, int x, int y, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return PluginManager.CLIENT_REGISTRY.createWidgets(registry, entries, x, y, functions, conditions);
    }

    @Override
    public <T extends LootItemCondition> List<Component> getConditionTooltip(IUtils utils, int pad, T condition) {
        return PluginManager.CLIENT_REGISTRY.getConditionTooltip(utils, pad, condition);
    }

    @Override
    public <T extends LootItemFunction> List<Component> getFunctionTooltip(IUtils utils, int pad, T function) {
        return PluginManager.CLIENT_REGISTRY.getFunctionTooltip(utils, pad, function);
    }

    @Override
    public <T extends LootPoolEntryContainer> List<Item> collectItems(IUtils utils, T entry) {
        return PluginManager.CLIENT_REGISTRY.collectItems(utils, entry);
    }

    @Override
    public Rect getBounds(IUtils registry, List<LootPoolEntryContainer> entries, int x, int y) {
        return PluginManager.CLIENT_REGISTRY.getBounds(registry, entries, x, y);
    }

    @Override
    public @Nullable LootTable getLootTable(ResourceLocation resourceLocation) {
        return PluginManager.CLIENT_REGISTRY.getLootTable(resourceLocation);
    }

    @Override
    public List<LootPool> getLootPools(LootTable lootTable) {
        return PluginManager.CLIENT_REGISTRY.getLootPools(lootTable);
    }

    @Nullable
    @Override
    public WidgetDirection getWidgetDirection(LootPoolEntryContainer entry) {
        return PluginManager.CLIENT_REGISTRY.getWidgetDirection(entry);
    }

    @Override
    public LootContext getLootContext() {
        return PluginManager.CLIENT_REGISTRY.getLootContext();
    }

    @Override
    public RangeValue convertNumber(IUtils utils, @Nullable NumberProvider numberProvider) {
        return PluginManager.CLIENT_REGISTRY.convertNumber(utils, numberProvider);
    }
}
