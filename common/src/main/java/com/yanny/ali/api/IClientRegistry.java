package com.yanny.ali.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

public interface IClientRegistry {
    void registerWidget(Identifier id, IWidgetFactory factory);

    <T extends IDataNode> void registerDataNode(Identifier id, DataFactory<T> dataFactory);

    <T extends ITooltipNode> void registerTooltipNode(Identifier id, TooltipFactory<T> tooltipFactory);

    @FunctionalInterface
    interface IWidgetFactory {
        IWidget create(IWidgetUtils registry, IDataNode entry, RelativeRect rect, int maxWidth);
    }

    @FunctionalInterface
    interface DataFactory<T extends IDataNode> {
        T create(IClientUtils utils, RegistryFriendlyByteBuf buf);
    }

    @FunctionalInterface
    interface TooltipFactory<T extends ITooltipNode> {
        T create(IClientUtils utils, RegistryFriendlyByteBuf buf);
    }
}
