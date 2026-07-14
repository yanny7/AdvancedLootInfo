package com.yanny.awi.plugin.client;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.manager.PluginManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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
}
