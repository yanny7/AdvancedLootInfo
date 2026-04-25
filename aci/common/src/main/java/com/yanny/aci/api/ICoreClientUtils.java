package com.yanny.aci.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public interface ICoreClientUtils
        <
                SU extends ICoreServerUtils,
                TN extends ICoreTooltipNode<SU>,
                DN extends ICoreDataNode<SU, TN>,
                CU extends ICoreClientUtils<SU, TN, DN, CU, WU>,
                WU extends ICoreWidgetUtils<SU, TN, DN>
        > {
    @NotNull
    List<IWidget> createWidgets(WU utils, List<DN> entries, RelativeRect parent, int maxWidth);

    @NotNull
    BiFunction<CU, FriendlyByteBuf, DN> getDataNodeFactory(ResourceLocation id);

    @NotNull
    BiFunction<CU, FriendlyByteBuf, TN> getTooltipNodeFactory(ResourceLocation id);
}
