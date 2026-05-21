package com.yanny.ali.manager;

import com.yanny.aci.manager.CorePluginManager;
import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.ICommonRegistry;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.platform.Services;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PluginManager extends CorePluginManager<ICommonRegistry, IServerRegistry, AliCommonRegistry, AliClientRegistry, AliServerRegistry, IClientRegistry, IPlugin> {
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
    protected AliCommonRegistry createCommonRegistry() {
        return new AliCommonRegistry();
    }

    @NotNull
    @Override
    protected AliClientRegistry createClientRegistry(AliCommonRegistry commonRegistry) {
        return new AliClientRegistry(commonRegistry);
    }

    @NotNull
    @Override
    protected AliServerRegistry createServerRegistry(AliCommonRegistry commonRegistry, ServerLevel level) {
        return new AliServerRegistry(commonRegistry, level);
    }
}
