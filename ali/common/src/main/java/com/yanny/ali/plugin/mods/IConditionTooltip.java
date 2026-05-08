package com.yanny.ali.plugin.mods;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;

public interface IConditionTooltip {
    TooltipNode getTooltip(IServerUtils utils);
}
