package com.yanny.ali.plugin;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
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
    public <T extends LootItemCondition> List<Component> getConditionTooltip(Class<T> clazz, IUtils utils, int pad, LootItemCondition condition) {
        return PluginManager.CLIENT_REGISTRY.getConditionTooltip(clazz, utils, pad, condition);
    }

    @Override
    public <T extends LootItemFunction> List<Component> getFunctionTooltip(Class<T> clazz, IUtils utils, int pad, LootItemFunction function) {
        return PluginManager.CLIENT_REGISTRY.getFunctionTooltip(clazz, utils, pad, function);
    }

    @Override
    public <T extends LootPoolEntryContainer> List<Item> collectItems(Class<T> clazz, IUtils utils, LootPoolEntryContainer entry) {
        return PluginManager.CLIENT_REGISTRY.collectItems(clazz, utils, entry);
    }

    @Override
    public Rect getBounds(IUtils registry, List<LootPoolEntryContainer> entries, int x, int y) {
        return PluginManager.CLIENT_REGISTRY.getBounds(registry, entries, x, y);
    }

    @Override
    public @Nullable LootTable getLootTable(Either<ResourceKey<LootTable>, LootTable> either) {
        return PluginManager.CLIENT_REGISTRY.getLootTable(either);
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
