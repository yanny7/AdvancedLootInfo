package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.ali.platform.Services;
import org.slf4j.Logger;

import java.util.List;

public class PluginManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static AliClientRegistry CLIENT_REGISTRY;
    public static AliServerRegistry SERVER_REGISTRY;
    private static List<PluginHolder> PLUGINS;

    public static void registerClientEvent() {
        registerClientData();
    }

    public static void registerCommonEvent() {
        PLUGINS = Services.PLATFORM.getPlugins();
        registerServerData();
    }

    private static void registerClientData() {
        LOGGER.info("Registering client plugin data...");
        CLIENT_REGISTRY = new AliClientRegistry();

        for (PluginHolder plugin : PLUGINS) {
            try {
                plugin.plugin().registerClient(CLIENT_REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to register {} client part with error: {}", plugin.modId(), throwable.getMessage());
            }
        }

        CLIENT_REGISTRY.printClientInfo();
        LOGGER.info("Registering client plugin data finished");
    }

    private static void registerServerData() {
        LOGGER.info("Registering server plugin data...");
        SERVER_REGISTRY = new AliServerRegistry();

        for (PluginHolder plugin : PLUGINS) {
            try {
                plugin.plugin().registerServer(SERVER_REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to register {} server part with error: {}", plugin.modId(), throwable.getMessage());
            }
        }

        SERVER_REGISTRY.printServerInfo();
        LOGGER.info("Registering server plugin data finished");
    }
}
