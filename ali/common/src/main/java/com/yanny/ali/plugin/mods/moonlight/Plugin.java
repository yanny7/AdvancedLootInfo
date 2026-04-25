package com.yanny.ali.plugin.mods.moonlight;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import org.jetbrains.annotations.NotNull;

// @AliEntrypoint
public class Plugin implements IPlugin {
    @NotNull
    @Override
    public String getModId() {
        return "moonlight";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerConditionTooltip(registry, OptionalPropertyCondition.class);
        PluginUtils.registerEntry(registry, OptionalItemPool.class);
    }
}
