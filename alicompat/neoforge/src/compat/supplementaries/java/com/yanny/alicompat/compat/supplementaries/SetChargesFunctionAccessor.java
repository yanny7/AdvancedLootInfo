package com.yanny.alicompat.compat.supplementaries;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.mehvahdjukaar.supplementaries.common.items.loot.SetChargesFunction;
import net.minecraft.util.valueproviders.IntProvider;
import org.jetbrains.annotations.NotNull;

public class SetChargesFunctionAccessor extends BaseAccessor<SetChargesFunction> implements IFunctionTooltip {
    @FieldAccessor
    private IntProvider amount;

    public SetChargesFunctionAccessor(SetChargesFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, new RangeValue(amount.getMinValue(), amount.getMaxValue())).build(Lang.Value.AMOUNT));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, SupplementariesLang.Functions.SET_CHARGES);
    }
}
