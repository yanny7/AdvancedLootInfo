package com.yanny.ali.plugin.mods.supplementaries.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

@ClassAccessor("net.mehvahdjukaar.supplementaries.common.items.loot.SetChargesFunction")
public class SetChargesFunction extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private IntProvider amount;

    public SetChargesFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.curse_loot"));

        tooltip.add(getStringTooltip(utils, "ali.property.value.amount", RangeValue.rangeToString(new RangeValue(amount.getMinValue()), new RangeValue(amount.getMaxValue()))));
        tooltip.add(getSubConditionsTooltip(utils, predicates));

        return tooltip;
    }
}
