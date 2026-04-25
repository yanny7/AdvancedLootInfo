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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public abstract class ClientUtils implements IWidgetUtils, IClientUtils {
    @NotNull
    @Override
    public List<IWidget> createWidgets(IWidgetUtils registry, List<IDataNode> entries, RelativeRect parent, int maxWidth) {
        return PluginManager.getInstance().clientRegistry.createWidgets(registry, entries, parent, maxWidth);
    }

    @NotNull
    @Override
    public BiFunction<IClientUtils, RegistryFriendlyByteBuf, IDataNode> getDataNodeFactory(ResourceLocation id) {
        return PluginManager.getInstance().clientRegistry.getDataNodeFactory(id);
    }

    @NotNull
    @Override
    public BiFunction<IClientUtils, RegistryFriendlyByteBuf, ITooltipNode> getTooltipNodeFactory(ResourceLocation id) {
        return PluginManager.getInstance().clientRegistry.getTooltipNodeFactory(id);
    }

    @NotNull
    @Override
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        return PluginManager.getInstance().clientRegistry.createEntities(type, level);
    }

    @NotNull
    @Override
    public AliConfig getConfiguration() {
        return PluginManager.getInstance().clientRegistry.getConfiguration();
    }
}
