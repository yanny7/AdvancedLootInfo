package com.yanny.alicompat.compat.portinglib;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IConditionTooltip;
import io.github.fabricators_of_create.porting_lib.tool.ToolAction;
import io.github.fabricators_of_create.porting_lib.tool.loot.CanToolPerformAction;
import org.jetbrains.annotations.NotNull;

public class CanToolPerformActionAccessor extends BaseAccessor<CanToolPerformAction> implements IConditionTooltip {
    @FieldAccessor
    private ToolAction action;

    public CanToolPerformActionAccessor(CanToolPerformAction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, action.name())), Lang.Conditions.CAN_TOOL_PERFORM_ACTION);
    }
}
