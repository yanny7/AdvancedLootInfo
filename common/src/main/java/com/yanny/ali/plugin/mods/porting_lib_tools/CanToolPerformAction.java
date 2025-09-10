package com.yanny.ali.plugin.mods.porting_lib_tools;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import com.yanny.ali.plugin.server.GenericTooltipUtils;

@ClassAccessor("io.github.fabricators_of_create.porting_lib.tool.loot.CanToolPerformAction")
public class CanToolPerformAction implements IConditionTooltip {
    @FieldAccessor(value = "action", clazz = ToolAction.class)
    public ToolAction action;

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return GenericTooltipUtils.getStringTooltip(utils, "ali.type.condition.can_tool_perform_action", action.name);
    }

    @ClassAccessor("io.github.fabricators_of_create.porting_lib.tool.ToolAction")
    public static class ToolAction {
        @FieldAccessor("name")
        public String name;
    }
}
