package com.yanny.aci.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.BiFunction;

public interface ICommonClientUtils<SU extends ICommonServerUtils, TN extends ICommonTooltipNode<SU>, DN extends ICommonDataNode<SU, TN>, CU extends ICommonClientUtils<SU, TN, DN, CU, WU>, WU extends ICommonWidgetUtils<SU, TN, DN>> {
    List<IWidget> createWidgets(WU registry, List<DN> entries, RelativeRect parent, int maxWidth);

    BiFunction<CU, RegistryFriendlyByteBuf, DN> getDataNodeFactory(Identifier id);

    BiFunction<CU, RegistryFriendlyByteBuf, TN> getTooltipNodeFactory(Identifier id);
}
