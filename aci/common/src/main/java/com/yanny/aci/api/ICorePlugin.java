package com.yanny.aci.api;

import org.jetbrains.annotations.NotNull;

public interface ICorePlugin
        <
                SU extends ICoreServerUtils,
                TN extends ICoreTooltipNode<SU>,
                DN extends ICoreDataNode<SU, TN>,
                CU extends ICoreClientUtils<SU, TN, DN, CU, WU>,
                WU extends ICoreWidgetUtils<SU, TN, DN>,
                BRE,
                CRE extends ICoreClientRegistry<SU, TN, DN, CU, WU>,
                SRE
        > {
    @NotNull
    String getModId();

    default void registerCommon(BRE registry) {}

    default void registerClient(CRE registry) {}

    default void registerServer(SRE registry) {}
}
