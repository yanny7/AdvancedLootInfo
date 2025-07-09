package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.ali.platform.Services;
import org.slf4j.Logger;

import java.util.List;

public class PluginManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static AliClientRegistry CLIENT_REGISTRY;
    public static AliServerRegistry SERVER_REGISTRY;
    public static AliCommonRegistry COMMON_REGISTRY;
    private static List<PluginHolder> PLUGINS;

    public static void registerClientEvent() {
        registerClientData();
    }

    public static void registerCommonEvent() {
        LOGGER.info("Registering common plugin data...");
        PLUGINS = Services.PLATFORM.getPlugins();
        COMMON_REGISTRY = new AliCommonRegistry();

        for (PluginHolder plugin : PLUGINS) {
            try {
                plugin.plugin().registerCommon(COMMON_REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to register {} common part with error: {}", plugin.modId(), throwable.getMessage());
            }
        }

        COMMON_REGISTRY.printClientInfo();
        LOGGER.info("Registering common plugin data finished");
    }

    public static void registerServerEvent() {
        registerServerData();
    }

    private static void registerClientData() {
        LOGGER.info("Registering client plugin data...");
        CLIENT_REGISTRY = new AliClientRegistry(COMMON_REGISTRY);

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
        SERVER_REGISTRY = new AliServerRegistry(COMMON_REGISTRY);

        for (PluginHolder plugin : PLUGINS) {
            try {
                plugin.plugin().registerServer(SERVER_REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to register {} server part with error: {}", plugin.modId(), throwable.getMessage());
            }
        }

        SERVER_REGISTRY.prepareLootModifiers();
        SERVER_REGISTRY.printServerInfo();
        LOGGER.info("Registering server plugin data finished");
    }
}
