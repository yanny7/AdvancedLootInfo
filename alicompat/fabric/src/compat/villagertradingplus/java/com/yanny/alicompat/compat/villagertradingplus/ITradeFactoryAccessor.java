package com.yanny.alicompat.compat.villagertradingplus;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import org.jetbrains.annotations.NotNull;

public interface ITradeFactoryAccessor {
    @NotNull
    IDataNode getNode(IServerUtils utils, TooltipNode conditions);
}
