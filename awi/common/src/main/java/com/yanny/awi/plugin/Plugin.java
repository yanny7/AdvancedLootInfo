package com.yanny.awi.plugin;

import com.yanny.awi.api.AwiEntrypoint;
import com.yanny.awi.api.IPlugin;
import com.yanny.awi.api.IServerRegistry;

@AwiEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "awi";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        IPlugin.super.registerServer(registry);
    }
}
