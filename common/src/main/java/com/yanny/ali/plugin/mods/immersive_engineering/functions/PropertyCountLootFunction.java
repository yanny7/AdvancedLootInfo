package com.yanny.ali.plugin.mods.immersive_engineering.functions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

@ClassAccessor("blusunrize.immersiveengineering.common.util.loot.PropertyCountLootFunction")
public class PropertyCountLootFunction extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private String propertyName;

    public PropertyCountLootFunction(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.property_count"));

        tooltip.add(getStringTooltip(utils, "ali.property.value.name", propertyName));
        tooltip.add(getSubConditionsTooltip(utils, predicates));

        return tooltip;
    }
}
