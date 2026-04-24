package com.yanny.aci.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;

public interface ICommonClientRegistry<SU extends ICommonServerUtils, TN extends ICommonTooltipNode<SU>, DN extends ICommonDataNode<SU, TN>, CU extends ICommonClientUtils<SU, TN, DN, CU, WU>, WU extends ICommonWidgetUtils<SU, TN, DN>> {
    void registerWidget(ResourceLocation id, IWidgetFactory<SU, TN, DN, WU> factory);

    void registerDataNode(ResourceLocation id, BiFunction<CU, FriendlyByteBuf, DN> dataFactory);

    void registerTooltipNode(ResourceLocation id, BiFunction<CU, FriendlyByteBuf, TN> tooltipFactory);

    @FunctionalInterface
    interface IWidgetFactory<SU extends ICommonServerUtils, T extends ICommonTooltipNode<SU>, DN extends ICommonDataNode<SU, T>, WU extends ICommonWidgetUtils<SU, T, DN>> {
        IWidget create(WU registry, DN entry, RelativeRect rect, int maxWidth);
    }
}
