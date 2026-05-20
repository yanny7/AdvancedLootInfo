package com.yanny.awi.manager;

import com.yanny.aci.manager.CorePluginManager;
import com.yanny.awi.api.IClientRegistry;
import com.yanny.awi.api.ICommonRegistry;
import com.yanny.awi.api.IPlugin;
import com.yanny.awi.api.IServerRegistry;
import com.yanny.awi.platform.Services;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PluginManager extends CorePluginManager<ICommonRegistry, IServerRegistry, AwiCommonRegistry, AwiClientRegistry, AwiServerRegistry, IClientRegistry, IPlugin> {
    private static PluginManager pluginManager;

    public static PluginManager getInstance() {
        if (pluginManager == null) {
            pluginManager = new PluginManager();
        }

        return pluginManager;
    }

    @NotNull
    @Override
    protected List<IPlugin> getPlugins() {
        return Services.getPlatform().getPlugins();
    }

    @NotNull
    @Override
    protected AwiCommonRegistry createCommonRegistry() {
        return new AwiCommonRegistry();
    }

    @NotNull
    @Override
    protected AwiClientRegistry createClientRegistry(AwiCommonRegistry commonRegistry) {
        return new AwiClientRegistry(commonRegistry);
    }

    @NotNull
    @Override
    protected AwiServerRegistry createServerRegistry(AwiCommonRegistry commonRegistry, ServerLevel level) {
        return new AwiServerRegistry(commonRegistry, level);
    }
}
