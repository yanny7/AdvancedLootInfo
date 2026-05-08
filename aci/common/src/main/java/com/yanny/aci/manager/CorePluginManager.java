package com.yanny.aci.manager;

import com.mojang.logging.LogUtils;
import com.yanny.aci.api.ICoreClientRegistry;
import com.yanny.aci.api.ICorePlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public abstract class CorePluginManager<
        TCommonRegistry,
        TServerRegistry,
        TCoreCommonRegistry extends CoreCommonRegistry<?>,
        TCoreClientRegistry extends CoreClientRegistry<?, ?, ?, ?, ?>,
        TCoreServerRegistry extends CoreServerRegistry<?, ?, ?>,
        TClientRegistry     extends ICoreClientRegistry<?, ?, ?>,
        TPlugin             extends ICorePlugin<TCommonRegistry, TClientRegistry, TServerRegistry>
        > {
    private static final Logger LOGGER = LogUtils.getLogger();

    public TCoreCommonRegistry commonRegistry;
    public TCoreClientRegistry clientRegistry;
    public TCoreServerRegistry serverRegistry;
    private List<TPlugin> plugins;

    @NotNull
    protected abstract List<TPlugin> getPlugins();

    @NotNull
    protected abstract TCoreCommonRegistry createCommonRegistry();

    @NotNull
    protected abstract TCoreClientRegistry createClientRegistry(TCoreCommonRegistry commonRegistry);

    @NotNull
    protected abstract TCoreServerRegistry createServerRegistry(TCoreCommonRegistry commonRegistry);

    public final void registerClientEvent() {
        registerClientData();
    }

    public final void registerCommonEvent() {
        plugins = getPlugins();

        LOGGER.info("Registering common plugin data...");
        commonRegistry = createCommonRegistry();

        for (TPlugin plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerCommon((TCommonRegistry) commonRegistry);
            } catch (Throwable e) {
                LOGGER.error("Failed to register {} common part with error: {}", plugin.getModId(), e.getMessage(), e);
            }
        }

        commonRegistry.printRegistrationInfo();
        LOGGER.info("Registering common plugin data finished");
    }

    public final void registerServerEvent() {
        registerServerData();
    }

    public final void reloadServer() {
        LOGGER.info("Reloading server plugin data...");
        serverRegistry.clearData();

        for (TPlugin plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerServer((TServerRegistry) serverRegistry);
            } catch (Throwable e) {
                LOGGER.error("Failed to reload {} server part with error: {}", plugin.getModId(), e.getMessage(), e);
            }
        }

        serverRegistry.printRegistrationInfo();
        LOGGER.info("Reloading server plugin data finished");
    }

    public final void deregisterServerEvent() {
        LOGGER.info("Deregistering server plugin data...");
        serverRegistry.clearData();
        serverRegistry = null;
        LOGGER.info("Deregistering server plugin data finished");
    }

    private void registerClientData() {
        LOGGER.info("Registering client plugin data...");
        clientRegistry = createClientRegistry(commonRegistry);

        for (TPlugin plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerClient((TClientRegistry) clientRegistry);
            } catch (Throwable e) {
                LOGGER.error("Failed to register {} client part with error: {}", plugin.getModId(), e.getMessage(), e);
            }
        }

        clientRegistry.printRegistrationInfo();
        LOGGER.info("Registering client plugin data finished");
    }

    private void registerServerData() {
        LOGGER.info("Registering server plugin data...");
        serverRegistry = createServerRegistry(commonRegistry);

        for (TPlugin plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerServer((TServerRegistry) serverRegistry);
            } catch (Throwable e) {
                LOGGER.error("Failed to register {} server part with error: {}", plugin.getModId(), e.getMessage(), e);
            }
        }

        serverRegistry.printRegistrationInfo();
        LOGGER.info("Registering server plugin data finished");
    }
}
