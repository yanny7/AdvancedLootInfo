package com.yanny.alicompat.accessor;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;

import java.util.List;

public interface IChanceModifier {
    NumberExpr applyChanceModifier(IServerUtils utils, NumberExpr chance, List<TooltipNode> conditions);
}
