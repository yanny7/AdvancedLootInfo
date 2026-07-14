package com.yanny.aci.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface ICoreClientRegistry<
        TDataNode    extends ICoreDataNode<?>,
        TWidgetUtils extends ICoreWidgetUtils<?, ?, ?, ?>,
        TClientUtils extends ICoreClientUtils<?, ?, ?>
        > {
    void registerWidget(ResourceLocation id, IWidgetFactory<TDataNode, TWidgetUtils> factory);

    void registerDataNode(ResourceLocation id, BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode> dataFactory);

    @FunctionalInterface
    interface IWidgetFactory<
            TDataNode    extends ICoreDataNode<?>,
            TWidgetUtils extends ICoreWidgetUtils<?, ?, ?, ?>
            > {
        @NotNull
        IWidget create(TWidgetUtils registry, TDataNode entry, RelativeRect rect, int maxWidth);
    }
}
