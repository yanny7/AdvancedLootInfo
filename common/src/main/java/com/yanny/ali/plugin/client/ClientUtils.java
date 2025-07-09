package com.yanny.ali.plugin.client;

import com.yanny.ali.api.*;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public abstract class ClientUtils implements IWidgetUtils {
    @Override
    public List<IWidget> createWidgets(IWidgetUtils registry, List<IDataNode> entries, RelativeRect parent, int maxWidth) {
        return PluginManager.CLIENT_REGISTRY.createWidgets(registry, entries, parent, maxWidth);
    }

    @Override
    public <T extends IDataNode> IClientRegistry.NodeFactory<T> getNodeFactory(ResourceLocation id) {
        return PluginManager.CLIENT_REGISTRY.getNodeFactory(id);
    }

    @Override
    public List<LootPool> getLootPools(LootTable lootTable) {
        return PluginManager.CLIENT_REGISTRY.getLootPools(lootTable);
    }

    @Override
    public List<Item> getItems(ResourceLocation location) {
        return PluginManager.CLIENT_REGISTRY.getItems(location);
    }
}
