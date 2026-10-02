package com.yanny.alicompat.accessor;

import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IServerUtils;

public interface IChanceModifier {
    NumberExpr applyChanceModifier(IServerUtils utils, NumberExpr chance);
}
