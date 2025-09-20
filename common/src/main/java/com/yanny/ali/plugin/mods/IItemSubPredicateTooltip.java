package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;

public interface IItemSubPredicateTooltip {
    ITooltipNode getTooltip(IServerUtils utils);
}