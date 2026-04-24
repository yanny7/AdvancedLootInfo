package com.yanny.ali.api;

import com.yanny.aci.api.ICommonKeyTooltipNode;

public interface IKeyTooltipNode extends ICommonKeyTooltipNode<IServerUtils, ITooltipNode> {
    @Override
    IKeyTooltipNode add(ITooltipNode node);
}
