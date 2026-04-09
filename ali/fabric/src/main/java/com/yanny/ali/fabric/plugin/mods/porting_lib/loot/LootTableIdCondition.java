package com.yanny.ali.fabric.plugin.mods.porting_lib.loot;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import net.minecraft.resources.ResourceLocation;

@ClassAccessor("io.github.fabricators_of_create.porting_lib.loot.LootTableIdCondition")
public class LootTableIdCondition extends BaseAccessor<Object> implements IConditionTooltip {
    @FieldAccessor
    private ResourceLocation targetLootTableId;

    public LootTableIdCondition(Object parent) {
        super(parent);
    }

    public ResourceLocation getTargetLootTableId() {
        return targetLootTableId;
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return utils.getValueTooltip(utils, targetLootTableId).build("ali.type.condition.loot_table_id");
    }
}
