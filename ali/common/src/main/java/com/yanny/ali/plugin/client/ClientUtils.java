package com.yanny.ali.plugin.client;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.BiFunction;

public abstract class ClientUtils implements IWidgetUtils, IClientUtils {
    @Override
    public List<IWidget> createWidgets(IWidgetUtils registry, List<IDataNode> entries, RelativeRect parent, int maxWidth) {
        return PluginManager.CLIENT_REGISTRY.createWidgets(registry, entries, parent, maxWidth);
    }

    @Override
    public BiFunction<IClientUtils, RegistryFriendlyByteBuf, IDataNode> getDataNodeFactory(Identifier id) {
        return PluginManager.CLIENT_REGISTRY.getDataNodeFactory(id);
    }

    @Override
    public BiFunction<IClientUtils, RegistryFriendlyByteBuf, ITooltipNode> getTooltipNodeFactory(Identifier id) {
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
