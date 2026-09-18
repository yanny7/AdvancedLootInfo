package com.yanny.alicompat.compat.goblintraders;

import com.mrcrayfish.goblintraders.loot_functions.IncreaseDurabilityFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import org.jetbrains.annotations.NotNull;

public class IncreaseDurabilityFunctionAccessor extends BaseAccessor<IncreaseDurabilityFunction> implements IFunctionTooltip {
    @FieldAccessor
    private double durabilityScale;

    public IncreaseDurabilityFunctionAccessor(IncreaseDurabilityFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, durabilityScale).build(GoblinTradersLang.Value.DURABILITY_SCALE));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, GoblinTradersLang.Functions.INCREASE_DURABILITY);
    }
}
