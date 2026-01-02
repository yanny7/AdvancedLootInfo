package com.yanny.ali.forge.plugin.mods.mantle.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("slimeknights.mantle.loot.function.SetFluidLootFunction")
public class SetFluidLootFunction extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private FluidStack fluid;

    public SetFluidLootFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fluid.getFluid()).build("ali.property.value.fluid"))
                .add(utils.getValueTooltip(utils, fluid.getAmount()).build("ali.property.value.amount"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(predicates)).build("ali.property.branch.conditions"))
                .build("ali.type.function.set_fluid");
    }
}
