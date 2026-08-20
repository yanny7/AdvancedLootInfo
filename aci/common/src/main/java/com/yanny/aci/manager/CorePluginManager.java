package com.yanny.aci.manager;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.ICoreClientRegistry;
import com.yanny.aci.api.ICorePlugin;
import net.minecraft.server.level.ServerLevel;
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
    private final Logger logger;

    public TCoreCommonRegistry commonRegistry;
    public TCoreClientRegistry clientRegistry;
    public TCoreServerRegistry serverRegistry;
    private List<TPlugin> plugins;

    protected CorePluginManager(String modId) {
        logger = CommonLogUtils.getLogger(modId);
    }

    @NotNull
    protected abstract List<TPlugin> getPlugins();

    @NotNull
    protected abstract TCoreCommonRegistry createCommonRegistry();

    @NotNull
    protected abstract TCoreClientRegistry createClientRegistry(TCoreCommonRegistry commonRegistry);

    @NotNull
    protected abstract TCoreServerRegistry createServerRegistry(TCoreCommonRegistry commonRegistry, ServerLevel level);

    public final void registerClientEvent() {
        registerClientData();
    }

    public final void registerCommonEvent() {
        plugins = getPlugins();

        logger.info("Registering common plugin data...");
        commonRegistry = createCommonRegistry();

        for (TPlugin plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerCommon((TCommonRegistry) commonRegistry);
            } catch (Throwable e) {
                logger.error("Failed to register {} common part with error: {}", plugin.getModId(), e.getMessage(), e);
            }
        }

        commonRegistry.printRegistrationInfo();
        logger.info("Registering common plugin data finished");
    }

    public final void registerServerEvent(ServerLevel level) {
        registerServerData(level);
    }

    public final void reloadServer() {
        logger.info("Reloading server plugin data...");
        serverRegistry.clearData();

        for (TPlugin plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerServer((TServerRegistry) serverRegistry);
            } catch (Throwable e) {
                logger.error("Failed to reload {} server part with error: {}", plugin.getModId(), e.getMessage(), e);
            }
        }

        serverRegistry.printRegistrationInfo();
        logger.info("Reloading server plugin data finished");
    }

    public final void deregisterServerEvent() {
        logger.info("Deregistering server plugin data...");
        serverRegistry.clearData();
        serverRegistry = null;
        logger.info("Deregistering server plugin data finished");
    }

    private void registerClientData() {
        logger.info("Registering client plugin data...");
        clientRegistry = createClientRegistry(commonRegistry);

        for (TPlugin plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerClient((TClientRegistry) clientRegistry);
            } catch (Throwable e) {
                logger.error("Failed to register {} client part with error: {}", plugin.getModId(), e.getMessage(), e);
            }
        }

        clientRegistry.printRegistrationInfo();
        logger.info("Registering client plugin data finished");
    }

    private void registerServerData(ServerLevel level) {
        logger.info("Registering server plugin data...");
        serverRegistry = createServerRegistry(commonRegistry, level);

        for (TPlugin plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerServer((TServerRegistry) serverRegistry);
            } catch (Throwable e) {
                logger.error("Failed to register {} server part with error: {}", plugin.getModId(), e.getMessage(), e);
            }
        }

        serverRegistry.printRegistrationInfo();
        logger.info("Registering server plugin data finished");
    }
}
