package com.yanny.ali.neoforge.plugin.mods.farmers_delight;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("vectorwing.farmersdelight.common.loot.function.SmokerCookFunction")
public class SmokerCookFunction extends ConditionalFunction implements IFunctionTooltip {
    public SmokerCookFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public TooltipNode getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b
                        .add(getSubConditionsTooltip(utils, predicates).build("ali.property.branch.conditions"))
                )
                .build("ali.type.function.smoker_cook");
    }
}
