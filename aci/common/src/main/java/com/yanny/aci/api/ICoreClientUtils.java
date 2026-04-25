package com.yanny.aci.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
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
    BiFunction<CU, RegistryFriendlyByteBuf, DN> getDataNodeFactory(Identifier id);

    @NotNull
    BiFunction<CU, RegistryFriendlyByteBuf, TN> getTooltipNodeFactory(Identifier id);
}
