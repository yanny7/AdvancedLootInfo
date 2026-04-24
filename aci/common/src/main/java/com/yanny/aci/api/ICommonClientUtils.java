package com.yanny.aci.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.BiFunction;

public interface ICommonClientUtils<SU extends ICommonServerUtils, TN extends ICommonTooltipNode<SU>, DN extends ICommonDataNode<SU, TN>, CU extends ICommonClientUtils<SU, TN, DN, CU, WU>, WU extends ICommonWidgetUtils<SU, TN, DN>> {
    List<IWidget> createWidgets(WU registry, List<DN> entries, RelativeRect parent, int maxWidth);

    BiFunction<CU, FriendlyByteBuf, DN> getDataNodeFactory(ResourceLocation id);

    BiFunction<CU, FriendlyByteBuf, TN> getTooltipNodeFactory(ResourceLocation id);
}
