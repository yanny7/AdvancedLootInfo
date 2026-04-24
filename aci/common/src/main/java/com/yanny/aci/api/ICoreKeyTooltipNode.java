package com.yanny.aci.api;

public interface ICoreKeyTooltipNode<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>, KTN extends ICoreKeyTooltipNode<SU, TN, KTN>> {
    KTN add(TN node);

    TN build(String key);
}
