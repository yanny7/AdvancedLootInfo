package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;
import twilightforest.loot.conditions.GiantPickUsedCondition;

public class GiantPickUsedConditionAccessor extends BaseAccessor<GiantPickUsedCondition> implements IConditionTooltip {
    @FieldAccessor
    private LootContext.EntityTarget entityTarget;

    public GiantPickUsedConditionAccessor(GiantPickUsedCondition parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, entityTarget)), TwilightForestLang.Conditions.GIANT_PICK_USED);
    }
}
