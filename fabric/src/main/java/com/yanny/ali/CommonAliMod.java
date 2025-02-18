package com.yanny.ali;

import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.Client;
import com.yanny.ali.network.DistHolder;
import com.yanny.ali.network.NetworkUtils;
import com.yanny.ali.network.Server;
import net.fabricmc.api.ModInitializer;

public class CommonAliMod implements ModInitializer {
    public static final DistHolder<Client, Server> INFO_PROPAGATOR;

    static {
        INFO_PROPAGATOR = NetworkUtils.registerLootInfoPropagator();
    }

    @Override
    public void onInitialize() {
        FabricBusSubscriber.registerEvents();
        NetworkUtils.registerLootInfoPropagator();
        PluginManager.registerCommonEvent();
    }
}
