package com.yanny.ali.fabric.plugin.mods.farmers_delight;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

@ClassAccessor("vectorwing.farmersdelight.common.loot.function.CopyMealFunction")
public class CopyMealFunction extends ConditionalFunction implements IFunctionTooltip {
    public CopyMealFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicates).build(Lang.Branch.CONDITIONS)))
                .key(Lang.Functions.COPY_MEAL);
    }
}
