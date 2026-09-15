package com.yanny.alicompat.compat.portinglib;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import io.github.fabricators_of_create.porting_lib.tool.ItemAbility;
import io.github.fabricators_of_create.porting_lib.tool.loot.CanItemPerformAbility;
import org.jetbrains.annotations.NotNull;

public class CanItemPerformAbilityAccessor extends BaseAccessor<CanItemPerformAbility> implements IConditionTooltip {
    @FieldAccessor
    private ItemAbility ability;

    public CanItemPerformAbilityAccessor(CanItemPerformAbility parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, ability.name())), Lang.Conditions.CAN_ITEM_PERFORM_ABILITY);
    }
}
