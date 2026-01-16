package com.yanny.ali.plugin.client;

import com.yanny.ali.api.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;

public abstract class ClientUtils implements IWidgetUtils {
    @Override
    public List<IWidget> createWidgets(IWidgetUtils registry, List<IDataNode> entries, RelativeRect parent, int maxWidth) {
        return PluginManager.CLIENT_REGISTRY.createWidgets(registry, entries, parent, maxWidth);
    }

    @Override
    public <T extends IDataNode> IClientRegistry.DataFactory<T> getDataNodeFactory(Identifier id) {
        return PluginManager.CLIENT_REGISTRY.getDataNodeFactory(id);
    }

    @Override
    public <T extends ITooltipNode> IClientRegistry.TooltipFactory<T> getTooltipNodeFactory(Identifier id) {
        return PluginManager.CLIENT_REGISTRY.getTooltipNodeFactory(id);
    }

    @Override
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        return PluginManager.CLIENT_REGISTRY.createEntities(type, level);
    }

    @Override
    public AliConfig getConfiguration() {
        return PluginManager.CLIENT_REGISTRY.getConfiguration();
    }
}
