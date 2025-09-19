package com.yanny.ali.plugin.mods.porting_lib_tools;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

@ClassAccessor("io.github.fabricators_of_create.porting_lib.tool.loot.CanToolPerformAction")
public class CanToolPerformAction extends BaseAccessor<LootItemCondition> implements IConditionTooltip {
    @FieldAccessor(clazz = ToolAction.class)
    public ToolAction action;

    public CanToolPerformAction(LootItemCondition parent) {
        super(parent);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return GenericTooltipUtils.getStringTooltip(utils, "ali.type.condition.can_tool_perform_action", action.name);
    }

    @ClassAccessor("io.github.fabricators_of_create.porting_lib.tool.ToolAction")
    public static class ToolAction extends BaseAccessor<Object> {
        @FieldAccessor
        public String name;

        public ToolAction(Object parent) {
            super(parent);
        }
    }
}
