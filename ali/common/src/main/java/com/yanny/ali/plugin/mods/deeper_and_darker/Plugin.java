package com.yanny.ali.plugin.mods.deeper_and_darker;

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
        return "deeperdarker";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, SetPaintingVariantFunction.class);
    }
}
