package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.platform.Services;
import org.slf4j.Logger;

import java.util.List;

public class PluginManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static AliClientRegistry CLIENT_REGISTRY;
    public static AliServerRegistry SERVER_REGISTRY;
    public static AliCommonRegistry COMMON_REGISTRY;
    private static List<IPlugin> PLUGINS;

    public static void registerClientEvent() {
        registerClientData();
    }

    public static void registerCommonEvent() {
        PLUGINS = Services.getPlatform().getPlugins();

        LOGGER.info("Registering common plugin data...");
        COMMON_REGISTRY = new AliCommonRegistry();
        COMMON_REGISTRY.loadConfiguration();

        for (IPlugin plugin : PLUGINS) {
            try {
                plugin.registerCommon(COMMON_REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to register {} common part with error: {}", plugin.getModId(), throwable.getMessage());
            }
        }

        COMMON_REGISTRY.printRegistrationInfo();
        LOGGER.info("Registering common plugin data finished");
    }

    public static void registerServerEvent() {
        registerServerData();
    }

    public static void reloadServer() {
        LOGGER.info("Reloading server plugin data...");
        SERVER_REGISTRY.clearData();

        for (IPlugin plugin : PLUGINS) {
            try {
                plugin.registerServer(SERVER_REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to reload {} server part with error: {}", plugin.getModId(), throwable.getMessage());
            }
        }

        SERVER_REGISTRY.prepareLootModifiers();
        SERVER_REGISTRY.printRegistrationInfo();
        LOGGER.info("Reloading server plugin data finished");
    }

    public static void deregisterServerEvent() {
        LOGGER.info("Deregistering server plugin data...");
        SERVER_REGISTRY.clearData();
        SERVER_REGISTRY = null;
        LOGGER.info("Deregistering server plugin data finished");
    }

    private static void registerClientData() {
        LOGGER.info("Registering client plugin data...");
        CLIENT_REGISTRY = new AliClientRegistry(COMMON_REGISTRY);

        for (IPlugin plugin : PLUGINS) {
            try {
                plugin.registerClient(CLIENT_REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to register {} client part with error: {}", plugin.getModId(), throwable.getMessage());
            }
        }

        CLIENT_REGISTRY.printRegistrationInfo();
        LOGGER.info("Registering client plugin data finished");
    }

    private static void registerServerData() {
        LOGGER.info("Registering server plugin data...");
        SERVER_REGISTRY = new AliServerRegistry(COMMON_REGISTRY);

        for (IPlugin plugin : PLUGINS) {
            try {
                plugin.registerServer(SERVER_REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to register {} server part with error: {}", plugin.getModId(), throwable.getMessage());
            }
        }

        SERVER_REGISTRY.prepareLootModifiers();
        SERVER_REGISTRY.printRegistrationInfo();
        LOGGER.info("Registering server plugin data finished");
    }
}
