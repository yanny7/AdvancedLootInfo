package com.yanny.alicompat.compat.portinglib;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import io.github.fabricators_of_create.porting_lib.loot.LootTableIdCondition;
import net.minecraft.resources.ResourceLocation;

public class LootTableIdConditionAccessor extends BaseAccessor<LootTableIdCondition> implements IConditionTooltip {
    @FieldAccessor
    private ResourceLocation targetLootTableId;

    public LootTableIdConditionAccessor(LootTableIdCondition parent) {
        super(parent);
    }

    public ResourceLocation getTargetLootTableId() {
        return targetLootTableId;
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return utils.getValueTooltip(utils, targetLootTableId).key(Lang.Conditions.LOOT_TABLE_ID);
    }
}
