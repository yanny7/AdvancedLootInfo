package com.yanny.awi.manager;

import com.mojang.logging.LogUtils;
import com.yanny.awi.api.IPlugin;
import com.yanny.awi.platform.Services;
import org.slf4j.Logger;

import java.util.List;

public class PluginManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static AwiServerRegistry SERVER_REGISTRY;
    private static List<IPlugin> PLUGINS;

    public static void registerServerEvent() {
        registerServerData();
    }

    public static void reloadServer() {
        LOGGER.info("Reloading server plugin data...");
        SERVER_REGISTRY.clearData();

        for (IPlugin plugin : PLUGINS) {
            try {
                plugin.registerServer(SERVER_REGISTRY);
            } catch (Throwable e) {
                LOGGER.error("Failed to reload {} server part with error: {}", plugin.getModId(), e.getMessage(), e);
            }
        }

        SERVER_REGISTRY.printRegistrationInfo();
        LOGGER.info("Reloading server plugin data finished");
    }

    public static void deregisterServerEvent() {
        LOGGER.info("Deregistering server plugin data...");
        SERVER_REGISTRY.clearData();
        SERVER_REGISTRY = null;
        LOGGER.info("Deregistering server plugin data finished");
    }

    private static void registerServerData() {
        LOGGER.info("Registering server plugin data...");
        SERVER_REGISTRY = new AwiServerRegistry();

        PLUGINS = Services.getPlatform().getPlugins();

        for (IPlugin plugin : PLUGINS) {
            try {
                plugin.registerServer(SERVER_REGISTRY);
            } catch (Throwable e) {
                LOGGER.error("Failed to register {} server part with error: {}", plugin.getModId(), e.getMessage(), e);
            }
        }

        SERVER_REGISTRY.printRegistrationInfo();
        LOGGER.info("Registering server plugin data finished");
    }
}
