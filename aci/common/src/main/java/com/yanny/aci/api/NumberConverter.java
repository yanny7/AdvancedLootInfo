package com.yanny.aci.api;

import com.yanny.aci.tooltip.TooltipNode;

import java.util.List;

@FunctionalInterface
public interface NumberConverter<U, T> {
    NumberExpr convert(U utils, T value, List<TooltipNode> conditions);
}
