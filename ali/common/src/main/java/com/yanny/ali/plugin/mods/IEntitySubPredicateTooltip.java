package com.yanny.ali.plugin.mods;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;

public interface IEntitySubPredicateTooltip {
    TooltipBuilder getTooltip(IServerUtils utils);
}