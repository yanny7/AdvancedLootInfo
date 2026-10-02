package com.yanny.ali.api;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;

import java.util.List;

@FunctionalInterface
public interface NumberModifier<T> {
    NumberExpr apply(IServerUtils utils, T value, NumberExpr number, List<TooltipNode> conditions);
}
