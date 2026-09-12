package com.yanny.alicompat.compat.supplementaries;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.mehvahdjukaar.supplementaries.common.items.loot.RandomArrowFunction;
import org.jetbrains.annotations.NotNull;

public class RandomArrowFunctionAccessor extends BaseAccessor<RandomArrowFunction> implements IFunctionTooltip {
    @FieldAccessor
    private int min;
    @FieldAccessor
    private int max;

    public RandomArrowFunctionAccessor(RandomArrowFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, new RangeValue(min, max)).build(Lang.Value.AMOUNT));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, SupplementariesLang.Functions.RANDOM_ARROWS);
    }
}
