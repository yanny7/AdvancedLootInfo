package com.yanny.ali.fabric.plugin.mods.porting_lib.loot;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import net.minecraft.resources.Identifier;

@ClassAccessor("io.github.fabricators_of_create.porting_lib.loot.LootTableIdCondition")
public class LootTableIdCondition extends BaseAccessor<Object> implements IConditionTooltip {
    @FieldAccessor
    private Identifier targetLootTableId;

    public LootTableIdCondition(Object parent) {
        super(parent);
    }

    public Identifier getTargetLootTableId() {
        return targetLootTableId;
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return utils.getValueTooltip(utils, targetLootTableId).key(Lang.Conditions.LOOT_TABLE_ID);
    }
}
