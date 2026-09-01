package com.yanny.alicompat.compat.portinglib;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.ReflectionUtils;
import com.yanny.alicompat.IModCompat;
import io.github.fabricators_of_create.porting_lib.tool.loot.CanToolPerformAction;
import org.jetbrains.annotations.NotNull;

public class PortingLibCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return "porting_lib_tool_actions";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(CanToolPerformAction.class, (utils, condition) ->
                ReflectionUtils.copyClassData(CanToolPerformActionAccessor.class, condition, CanToolPerformAction.class).getTooltip(utils));
    }
}
