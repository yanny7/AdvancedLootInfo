package com.yanny.aci.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public interface ICoreClientUtils<
        TDataNode    extends ICoreDataNode<?>,
        TWidgetUtils extends ICoreWidgetUtils<?>,
        SELF         extends ICoreClientUtils<?, ?, ?>
        > {
    @NotNull
    List<IWidget> createWidgets(TWidgetUtils utils, List<TDataNode> entries, RelativeRect parent, int maxWidth);

    @NotNull
    BiFunction<SELF, FriendlyByteBuf, TDataNode> getDataNodeFactory(ResourceLocation id);
}
