package com.yanny.aci.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public interface ICoreClientUtils<
        TTooltipNode extends ICoreTooltipNode<?>,
        TDataNode    extends ICoreDataNode<?, ?>,
        TWidgetUtils extends ICoreWidgetUtils<?>,
        SELF         extends ICoreClientUtils<?, ?, ?, ?>
        > {
    @NotNull
    List<IWidget> createWidgets(TWidgetUtils utils, List<TDataNode> entries, RelativeRect parent, int maxWidth);

    @NotNull
    BiFunction<SELF, RegistryFriendlyByteBuf, TDataNode> getDataNodeFactory(Identifier id);

    @NotNull
    BiFunction<SELF, RegistryFriendlyByteBuf, TTooltipNode> getTooltipNodeFactory(Identifier id);
}
