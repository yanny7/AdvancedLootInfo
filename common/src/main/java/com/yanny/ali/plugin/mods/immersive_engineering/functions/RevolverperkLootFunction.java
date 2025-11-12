package com.yanny.ali.plugin.mods.immersive_engineering.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("blusunrize.immersiveengineering.common.util.loot.RevolverperkLootFunction")
public class RevolverperkLootFunction extends ConditionalFunction implements IFunctionTooltip {
    public RevolverperkLootFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch()
                .add(getSubConditionsTooltip(utils, predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.revolver_perk");
    }
}
