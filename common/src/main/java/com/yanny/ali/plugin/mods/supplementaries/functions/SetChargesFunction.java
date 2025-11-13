package com.yanny.ali.plugin.mods.supplementaries.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("net.mehvahdjukaar.supplementaries.common.items.loot.SetChargesFunction")
public class SetChargesFunction extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private IntProvider amount;

    public SetChargesFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, RangeValue.rangeToString(new RangeValue(amount.getMinValue()), new RangeValue(amount.getMaxValue()))).build("ali.property.value.amount"))
                .add(getSubConditionsTooltip(utils, predicates).build("ali.property.branch.conditions"))
                .build("ali.type.function.curse_loot");
    }
}
