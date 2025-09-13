package com.yanny.ali.plugin.mods.the_bumblezone.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;

@ClassAccessor("com.telepathicgrunt.the_bumblezone.loot.functions.UniquifyIfHasItems")
public class UniquifyIfHasItems extends ConditionalFunction implements IFunctionTooltip {
    public UniquifyIfHasItems(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.uniquify_if_has_items"));

        tooltip.add(getSubConditionsTooltip(utils, predicates));

        return tooltip;
    }
}
