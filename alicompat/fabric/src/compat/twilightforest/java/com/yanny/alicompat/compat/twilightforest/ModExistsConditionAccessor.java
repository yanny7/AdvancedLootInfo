package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import org.jetbrains.annotations.NotNull;
import twilightforest.loot.conditions.ModExistsCondition;

public class ModExistsConditionAccessor extends BaseAccessor<ModExistsCondition> implements IConditionTooltip {
    @FieldAccessor
    private String modID;

    public ModExistsConditionAccessor(ModExistsCondition parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, modID)), TwilightForestLang.Conditions.MOD_EXISTS);
    }
}
