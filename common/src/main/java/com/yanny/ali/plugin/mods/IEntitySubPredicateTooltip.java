package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;

public interface IEntitySubPredicateTooltip {
    IKeyTooltipNode getTooltip(IServerUtils utils);
}