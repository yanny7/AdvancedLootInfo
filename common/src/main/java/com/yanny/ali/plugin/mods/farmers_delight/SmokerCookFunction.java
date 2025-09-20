package com.yanny.ali.plugin.mods.farmers_delight;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;

@ClassAccessor("vectorwing.farmersdelight.common.loot.function.SmokerCookFunction")
public class SmokerCookFunction extends ConditionalFunction implements IFunctionTooltip {
    public SmokerCookFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.smoker_cook"));

        tooltip.add(getSubConditionsTooltip(utils, predicates));

        return tooltip;
    }
}
