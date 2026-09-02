package com.yanny.alicompat.accessor;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;

public interface IEntitySubPredicateTooltip {
    TooltipBuilder getTooltip(IServerUtils utils);
}