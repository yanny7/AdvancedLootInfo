package com.yanny.ali.manager;

import com.yanny.aci.manager.CorePluginManager;
import com.yanny.ali.api.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.platform.Services;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PluginManager extends CorePluginManager<AliConfig, ICommonUtils, IServerUtils, ITooltipNode, IDataNode, IWidgetUtils, IClientUtils, AliCommonRegistry, AliClientRegistry, AliServerRegistry, ICommonRegistry, IClientRegistry, IServerRegistry, IPlugin> {
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
    protected AliServerRegistry createServerRegistry(AliCommonRegistry commonRegistry) {
        return new AliServerRegistry(commonRegistry);
    }
}
