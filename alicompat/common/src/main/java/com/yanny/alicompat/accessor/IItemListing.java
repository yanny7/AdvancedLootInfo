package com.yanny.alicompat.accessor;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;

public interface IItemListing {
    IDataNode getNode(IServerUtils utils, TooltipNode conditions);
}
