package com.yanny.alicompat.compat.immersiveengineering;

import com.yanny.aci.api.NumberExpr;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$PriceInterval")
public class PriceIntervalAccessor extends BaseAccessor<Object> {
    @FieldAccessor
    private int min;

    @FieldAccessor
    private int max;

    public PriceIntervalAccessor(Object parent) {
        super(parent);
    }

    @NotNull
    public NumberExpr getRange() {
        return NumberExpr.uniformInt(min, Math.max(min, max));
    }
}
