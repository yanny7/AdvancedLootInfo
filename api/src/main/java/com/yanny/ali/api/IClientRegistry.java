package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IClientRegistry {
    void registerWidget(ResourceLocation id, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter);

    <T extends IDataNode> void registerNode(ResourceLocation id, NodeFactory<T> nodeFactory);

    @FunctionalInterface
    interface IBoundsGetter {
        Rect apply(IClientUtils utils, IDataNode entry, int x, int y, int maxWidth);
    }

    @FunctionalInterface
    interface IWidgetFactory {
        IEntryWidget create(IWidgetUtils registry, IDataNode entry, int x, int y, int maxWidth);
    }

    @FunctionalInterface
    interface NodeFactory<T extends IDataNode> {
        T create(IClientUtils utils, FriendlyByteBuf buf);
    }
}
