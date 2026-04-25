package com.yanny.aci.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface ICoreClientRegistry
        <
                SU extends ICoreServerUtils,
                TN extends ICoreTooltipNode<SU>,
                DN extends ICoreDataNode<SU, TN>,
                CU extends ICoreClientUtils<SU, TN, DN, CU, WU>,
                WU extends ICoreWidgetUtils<SU, TN, DN>
        > {
    void registerWidget(Identifier id, IWidgetFactory<SU, TN, DN, WU> factory);

    void registerDataNode(Identifier id, BiFunction<CU, RegistryFriendlyByteBuf, DN> dataFactory);

    void registerTooltipNode(Identifier id, BiFunction<CU, RegistryFriendlyByteBuf, TN> tooltipFactory);

    @FunctionalInterface
    interface IWidgetFactory<SU extends ICoreServerUtils, T extends ICoreTooltipNode<SU>, DN extends ICoreDataNode<SU, T>, WU extends ICoreWidgetUtils<SU, T, DN>> {
        @NotNull
        IWidget create(WU registry, DN entry, RelativeRect rect, int maxWidth);
    }
}
