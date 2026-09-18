package com.yanny.alicompat.compat.mantle;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.loot.condition.HasLootContextSetCondition;

public class HasLootContextSetConditionAccessor extends BaseAccessor<HasLootContextSetCondition> implements IConditionTooltip {
    public HasLootContextSetConditionAccessor(HasLootContextSetCondition parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, parent.set()).build(Lang.Value.ID)), MantleLang.Conditions.HAS_LOOT_CONTEXT_SET);
    }
}
