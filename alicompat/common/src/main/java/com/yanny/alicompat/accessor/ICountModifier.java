package com.yanny.alicompat.accessor;

import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IServerUtils;

public interface ICountModifier {
    NumberExpr applyCountModifier(IServerUtils utils, NumberExpr count);
}
