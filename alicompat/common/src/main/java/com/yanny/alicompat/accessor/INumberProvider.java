package com.yanny.alicompat.accessor;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;

import java.util.List;

public interface INumberProvider {
    NumberExpr convertNumber(IServerUtils utils, List<TooltipNode> conditions);

    default NumberExpr convertIntNumber(IServerUtils utils, List<TooltipNode> conditions) {
        return NumberExpr.fn(NumberFunctions.ROUND, convertNumber(utils, conditions));
    }
}
