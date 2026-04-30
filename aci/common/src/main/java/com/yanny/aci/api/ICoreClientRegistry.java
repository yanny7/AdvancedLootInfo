package com.yanny.aci.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface ICoreClientRegistry<
        TTooltipNode extends ICoreTooltipNode<?>,
        TDataNode    extends ICoreDataNode<?, ?>,
        TWidgetUtils extends ICoreWidgetUtils<?>,
        TClientUtils extends ICoreClientUtils<?, ?, ?, ?>
        > {
    void registerWidget(Identifier id, IWidgetFactory<TDataNode, TWidgetUtils> factory);

    void registerDataNode(Identifier id, BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode> dataFactory);

    void registerTooltipNode(Identifier id, BiFunction<TClientUtils, RegistryFriendlyByteBuf, TTooltipNode> tooltipFactory);

    @FunctionalInterface
    interface IWidgetFactory<
            TDataNode    extends ICoreDataNode<?, ?>,
            TWidgetUtils extends ICoreWidgetUtils<?>
            > {
        @NotNull
        IWidget create(TWidgetUtils registry, TDataNode entry, RelativeRect rect, int maxWidth);
    }
}
