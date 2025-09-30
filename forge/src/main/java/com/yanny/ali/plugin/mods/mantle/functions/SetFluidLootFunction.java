package com.yanny.ali.plugin.mods.mantle.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraftforge.fluids.FluidStack;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

@ClassAccessor("slimeknights.mantle.loot.function.SetFluidLootFunction")
public class SetFluidLootFunction extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private FluidStack fluid;

    public SetFluidLootFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.set_fluid"));

        tooltip.add(RegistriesTooltipUtils.getFluidTooltip(utils, "ali.property.value.fluid", fluid.getFluid()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.amount", fluid.getAmount()));
        tooltip.add(getSubConditionsTooltip(utils, predicates));

        return tooltip;
    }
}
