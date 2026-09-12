package com.yanny.alicompat.compat.mantle;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.loot.function.SetFluidLootFunction;

public class SetFluidLootFunctionAccessor extends BaseAccessor<SetFluidLootFunction> implements IFunctionTooltip {
    @FieldAccessor
    private FluidStack fluid;

    public SetFluidLootFunctionAccessor(SetFluidLootFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fluid).build(Lang.Branch.FLUID));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, MantleLang.Functions.SET_FLUID);
    }
}
