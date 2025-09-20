package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;

public interface IEntitySubPredicate {
    ITooltipNode getTooltip(IServerUtils utils);
}