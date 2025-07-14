package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IClientRegistry {
    void registerWidget(ResourceLocation id, IWidgetFactory factory);

    <T extends IDataNode> void registerNode(ResourceLocation id, NodeFactory<T> nodeFactory);

    @FunctionalInterface
    interface IWidgetFactory {
        IWidget create(IWidgetUtils registry, IDataNode entry, RelativeRect rect, int maxWidth);
    }

    @FunctionalInterface
    interface NodeFactory<T extends IDataNode> {
        T create(IClientUtils utils, FriendlyByteBuf buf);
    }

    @FunctionalInterface
    interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }
}
