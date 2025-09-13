package com.yanny.ali.plugin.mods.porting_lib_tools;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;

//@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "porting_lib_tool_actions";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerConditionTooltip(registry, CanToolPerformAction.class);
    }
}
