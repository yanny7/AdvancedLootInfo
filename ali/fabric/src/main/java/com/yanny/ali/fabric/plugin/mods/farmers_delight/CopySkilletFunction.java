package com.yanny.ali.fabric.plugin.mods.farmers_delight;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("vectorwing.farmersdelight.common.loot.function.CopySkilletFunction")
public class CopySkilletFunction extends ConditionalFunction implements IFunctionTooltip {
    public CopySkilletFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(getSubConditionsTooltip(utils, Arrays.asList(predicates)).build("ali.property.branch.conditions")))
                .key("ali.type.function.copy_skillet");
    }
}
