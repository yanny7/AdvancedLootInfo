package com.yanny.awi.api;

import com.yanny.aci.api.ICommonKeyTooltipNode;

public interface IKeyTooltipNode extends ICommonKeyTooltipNode<IServerUtils, ITooltipNode> {
    @Override
    IKeyTooltipNode add(ITooltipNode node);
}
