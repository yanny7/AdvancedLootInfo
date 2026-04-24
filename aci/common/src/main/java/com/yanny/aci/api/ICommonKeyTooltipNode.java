package com.yanny.aci.api;

public interface ICommonKeyTooltipNode<SU extends ICommonServerUtils, TN extends ICommonTooltipNode<SU>> {
    ICommonKeyTooltipNode<SU, TN> add(TN node);

    TN build(String key);
}
