package com.yanny.awi.api;

import com.yanny.aci.api.ICoreKeyTooltipNode;
import org.jetbrains.annotations.NotNull;

public interface IKeyTooltipNode extends ICoreKeyTooltipNode<IServerUtils, ITooltipNode, IKeyTooltipNode> {
    @NotNull
    @Override
    IKeyTooltipNode add(ITooltipNode node);
}
