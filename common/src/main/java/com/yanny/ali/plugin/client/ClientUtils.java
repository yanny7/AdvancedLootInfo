package com.yanny.ali.plugin.client;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ClientUtils implements IWidgetUtils {
    @Override
    public Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<IDataNode> entries, int x, int y, int maxWidth) {
        return PluginManager.CLIENT_REGISTRY.createWidgets(registry, entries, x, y, maxWidth);
    }

    @Override
    public <T extends IDataNode> IClientRegistry.NodeFactory<T> getNodeFactory(ResourceLocation id) {
        return PluginManager.CLIENT_REGISTRY.getNodeFactory(id);
    }

    @Override
    public Rect getBounds(IClientUtils registry, List<IDataNode> entries, int x, int y, int maxWidth) {
        return PluginManager.CLIENT_REGISTRY.getBounds(registry, entries, x, y, maxWidth);
    }

    @Override
    public List<LootPool> getLootPools(LootTable lootTable) {
        return PluginManager.CLIENT_REGISTRY.getLootPools(lootTable);
    }

    @Nullable
    @Override
    public WidgetDirection getWidgetDirection(ResourceLocation id) {
        return PluginManager.CLIENT_REGISTRY.getWidgetDirection(id);
    }

    @Override
    public List<Item> getItems(ResourceLocation location) {
        return PluginManager.CLIENT_REGISTRY.getItems(location);
    }
}
