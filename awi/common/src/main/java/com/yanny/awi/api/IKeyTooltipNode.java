package com.yanny.awi.api;

import com.yanny.aci.api.ICoreKeyTooltipNode;

public interface IKeyTooltipNode extends ICoreKeyTooltipNode<IServerUtils, ITooltipNode, IKeyTooltipNode> {
    @Override
    IKeyTooltipNode add(ITooltipNode node);
}
