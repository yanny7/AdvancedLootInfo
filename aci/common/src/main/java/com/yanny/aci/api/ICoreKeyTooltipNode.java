package com.yanny.aci.api;

import org.jetbrains.annotations.NotNull;

public interface ICoreKeyTooltipNode
        <
                SU extends ICoreServerUtils,
                TN extends ICoreTooltipNode<SU>,
                KTN extends ICoreKeyTooltipNode<SU, TN, KTN>
        > {
    @NotNull
    KTN add(TN node);

    @NotNull
    TN build(String key);
}
