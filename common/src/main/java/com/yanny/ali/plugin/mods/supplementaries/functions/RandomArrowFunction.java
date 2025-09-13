package com.yanny.ali.plugin.mods.supplementaries.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

@ClassAccessor("net.mehvahdjukaar.supplementaries.common.items.loot.RandomArrowFunction")
public class RandomArrowFunction extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private int min;
    @FieldAccessor
    private int max;

    public RandomArrowFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.random_arrow"));

        tooltip.add(getIntRangeTooltip(utils, "ali.property.value.amount", IntRange.range(min, max)));
        tooltip.add(getSubConditionsTooltip(utils, predicates));

        return tooltip;
    }
}
