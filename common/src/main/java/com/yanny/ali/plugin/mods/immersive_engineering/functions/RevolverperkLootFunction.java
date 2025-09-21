package com.yanny.ali.plugin.mods.immersive_engineering.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;

@ClassAccessor("blusunrize.immersiveengineering.common.util.loot.RevolverperkLootFunction")
public class RevolverperkLootFunction extends ConditionalFunction implements IFunctionTooltip {
    public RevolverperkLootFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.revolver_perk"));

        tooltip.add(getSubConditionsTooltip(utils, predicates));

        return tooltip;
    }
}
