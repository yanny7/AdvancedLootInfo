package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.ali.platform.Services;
import org.slf4j.Logger;

import java.util.List;

public class PluginManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static CommonAliRegistry COMMON_REGISTRY;
    public static ClientAliRegistry CLIENT_REGISTRY;
    private static List<PluginHolder> PLUGINS;

    public static void registerCommonEvent() {
        PLUGINS = Services.PLATFORM.getPlugins();
        registerCommonData();
    }

    public static void registerClientEvent() {
        registerClientData();
    }

    private static void registerCommonData() {
        COMMON_REGISTRY = new CommonAliRegistry();
        LOGGER.info("Registering common plugin data...");

        for (PluginHolder plugin : PLUGINS) {
            try {
                plugin.plugin().registerCommon(COMMON_REGISTRY);
            } catch (Throwable throwable) {
                LOGGER.error("Failed to register {} common part with error: {}", plugin.modId(), throwable.getMessage());
            }
        }

        COMMON_REGISTRY.printCommonInfo();
        LOGGER.info("Registering common plugin data finished");
    }

    private static void registerClientData() {
        LOGGER.info("Registering client plugin data...");
        CLIENT_REGISTRY = new ClientAliRegistry();

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
}
