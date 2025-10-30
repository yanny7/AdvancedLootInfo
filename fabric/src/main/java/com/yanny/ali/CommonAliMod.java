package com.yanny.ali;

import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DistHolder;
import com.yanny.ali.network.NetworkUtils;
import net.fabricmc.api.ModInitializer;

public class CommonAliMod implements ModInitializer {
    public static final DistHolder<AbstractClient, AbstractServer> INFO_PROPAGATOR;

    static {
        INFO_PROPAGATOR = NetworkUtils.registerLootInfoPropagator();
    }

    @Override
    public void onInitialize() {
        FabricBusSubscriber.registerEvents();
        PluginManager.registerCommonEvent();
    }
}
