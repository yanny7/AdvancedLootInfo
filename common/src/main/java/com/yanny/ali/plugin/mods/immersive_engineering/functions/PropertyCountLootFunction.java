package com.yanny.ali.plugin.mods.immersive_engineering.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("blusunrize.immersiveengineering.common.util.loot.PropertyCountLootFunction")
public class PropertyCountLootFunction extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private String propertyName;

    public PropertyCountLootFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch("ali.type.function.property_count")
                .add(utils.getValueTooltip(utils, propertyName).key("ali.property.value.name"))
                .add(getSubConditionsTooltip(utils, predicates));
    }
}
