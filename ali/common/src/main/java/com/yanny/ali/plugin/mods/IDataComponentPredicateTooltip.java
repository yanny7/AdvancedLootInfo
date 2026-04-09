package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;

public interface IDataComponentPredicateTooltip {
    ITooltipNode getTooltip(IServerUtils utils);
}