package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.util.WeightedList;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IValueTooltip;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WeightedListAccessor extends BaseAccessor<WeightedList<?>> implements IValueTooltip {
    @FieldAccessor
    private List<?> entries;

    public WeightedListAccessor(WeightedList<?> parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return utils.getValueTooltip(utils, entries);
    }
}
