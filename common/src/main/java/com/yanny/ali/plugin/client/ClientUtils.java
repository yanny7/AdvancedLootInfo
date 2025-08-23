package com.yanny.ali.plugin.client;

import com.yanny.ali.api.*;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        return PluginManager.CLIENT_REGISTRY.createEntities(type, level);
    }

    @Override
    public List<ItemStack> getLootItems(ResourceLocation location) {
        return PluginManager.CLIENT_REGISTRY.getLootItems(location);
    }

    @Override
    public List<Item> getTradeInputItems(ResourceLocation location) {
        return PluginManager.CLIENT_REGISTRY.getTradeInputItems(location);
    }

    @Override
    public List<Item> getTradeOutputItems(ResourceLocation location) {
        return PluginManager.CLIENT_REGISTRY.getTradeOutputItems(location);
    }
}
