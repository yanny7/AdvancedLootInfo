package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import twilightforest.loot.conditions.ModExistsCondition;

public class ModExistsConditionAccessor extends BaseAccessor<ModExistsCondition> implements IConditionTooltip {
    @FieldAccessor
    private String modID;

    public ModExistsConditionAccessor(ModExistsCondition parent) {
        super(parent);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return utils.getValueTooltip(utils, modID).key(TwilightForestLang.Conditions.MOD_EXISTS);
    }
}
