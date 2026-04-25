package com.yanny.awi.manager;

import com.yanny.aci.manager.CorePluginManager;
import com.yanny.awi.api.*;
import com.yanny.awi.platform.Services;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PluginManager extends CorePluginManager<Object, ICommonUtils, IServerUtils, ITooltipNode, IDataNode, IWidgetUtils, IClientUtils, AwiCommonRegistry, AwiClientRegistry, AwiServerRegistry, ICommonRegistry, IClientRegistry, IServerRegistry, IPlugin> {
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
    protected AwiServerRegistry createServerRegistry(AwiCommonRegistry commonRegistry) {
        return new AwiServerRegistry(commonRegistry);
    }
}
