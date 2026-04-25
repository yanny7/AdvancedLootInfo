package com.yanny.aci.manager;

import com.mojang.logging.LogUtils;
import com.yanny.aci.api.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public abstract class CorePluginManager
        <
                CN,
                BU extends ICoreCommonUtils<CN>,
                SU extends ICoreServerUtils,
                TN extends ICoreTooltipNode<SU>,
                DN extends ICoreDataNode<SU, TN>,
                WU extends ICoreWidgetUtils<SU, TN, DN>,
                CU extends ICoreClientUtils<SU, TN, DN, CU, WU>,
                BR extends CoreCommonRegistry<CN>,
                CR extends CoreClientRegistry<CN, BU, SU, TN, DN, WU, CU>,
                SR extends CoreServerRegistry<CN, BU>,
                BRE,
                CRE extends ICoreClientRegistry<SU, TN, DN, CU, WU>,
                SRE,
                P extends ICorePlugin<SU, TN, DN, CU, WU, BRE, CRE, SRE>
        > {
    private static final Logger LOGGER = LogUtils.getLogger();

    public BR commonRegistry;
    public CR clientRegistry;
    public SR serverRegistry;
    private List<P> plugins;

    @NotNull
    protected abstract List<P> getPlugins();

    @NotNull
    protected abstract BR createCommonRegistry();

    @NotNull
    protected abstract CR createClientRegistry(BR commonRegistry);

    @NotNull
    protected abstract SR createServerRegistry(BR commonRegistry);

    public final void registerClientEvent() {
        registerClientData();
    }

    public final void registerCommonEvent() {
        plugins = getPlugins();

        LOGGER.info("Registering common plugin data...");
        commonRegistry = createCommonRegistry();

        for (P plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerCommon((BRE) commonRegistry);
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

        for (P plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerServer((SRE) serverRegistry);
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

        for (P plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerClient((CRE) clientRegistry);
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

        for (P plugin : plugins) {
            try {
                //noinspection unchecked
                plugin.registerServer((SRE) serverRegistry);
            } catch (Throwable e) {
                LOGGER.error("Failed to register {} server part with error: {}", plugin.getModId(), e.getMessage(), e);
            }
        }

        serverRegistry.printRegistrationInfo();
        LOGGER.info("Registering server plugin data finished");
    }
}
