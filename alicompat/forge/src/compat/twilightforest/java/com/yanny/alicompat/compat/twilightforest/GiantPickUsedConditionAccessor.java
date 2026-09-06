package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import net.minecraft.world.level.storage.loot.LootContext;
import twilightforest.loot.conditions.GiantPickUsedCondition;

public class GiantPickUsedConditionAccessor extends BaseAccessor<GiantPickUsedCondition> implements IConditionTooltip {
    @FieldAccessor
    private LootContext.EntityTarget entityTarget;

    public GiantPickUsedConditionAccessor(GiantPickUsedCondition parent) {
        super(parent);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return utils.getValueTooltip(utils, entityTarget).key(TwilightForestLang.Conditions.GIANT_PICK_USED);
    }
}
