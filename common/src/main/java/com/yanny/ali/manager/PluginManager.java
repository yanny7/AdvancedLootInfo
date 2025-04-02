package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.ali.platform.Services;
import org.slf4j.Logger;

import java.util.List;

public class PluginManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static AliRegistry CLIENT_REGISTRY;
    private static List<PluginHolder> PLUGINS;

    public static void registerCommonEvent() {
        PLUGINS = Services.PLATFORM.getPlugins();
    }

    public static void registerClientEvent() {
        registerClientData();
    }

    private static void registerClientData() {
        LOGGER.info("Registering client plugin data...");
        CLIENT_REGISTRY = new AliRegistry();

        for (PluginHolder plugin : PLUGINS) {
            try {
                plugin.plugin().register(CLIENT_REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to register {} client part with error: {}", plugin.modId(), throwable.getMessage());
            }
        }

        CLIENT_REGISTRY.printClientInfo();
        LOGGER.info("Registering client plugin data finished");
    }
}
