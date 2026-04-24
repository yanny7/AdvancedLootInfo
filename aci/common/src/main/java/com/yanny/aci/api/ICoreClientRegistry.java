package com.yanny.aci.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;

public interface ICoreClientRegistry<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>, DN extends ICoreDataNode<SU, TN>, CU extends ICoreClientUtils<SU, TN, DN, CU, WU>, WU extends ICoreWidgetUtils<SU, TN, DN>> {
    void registerWidget(ResourceLocation id, IWidgetFactory<SU, TN, DN, WU> factory);

    void registerDataNode(ResourceLocation id, BiFunction<CU, FriendlyByteBuf, DN> dataFactory);

    void registerTooltipNode(ResourceLocation id, BiFunction<CU, FriendlyByteBuf, TN> tooltipFactory);

    @FunctionalInterface
    interface IWidgetFactory<SU extends ICoreServerUtils, T extends ICoreTooltipNode<SU>, DN extends ICoreDataNode<SU, T>, WU extends ICoreWidgetUtils<SU, T, DN>> {
        IWidget create(WU registry, DN entry, RelativeRect rect, int maxWidth);
    }
}
