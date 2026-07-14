package com.yanny.awi.fabric;

import com.yanny.awi.fabric.network.Client;
import com.yanny.awi.fabric.network.NetworkUtils;
import com.yanny.awi.manager.PluginManager;
import net.fabricmc.api.ClientModInitializer;

public class ClientAliMod implements ClientModInitializer {
    public static final Client CLIENT = new Client();

    @Override
    public void onInitializeClient() {
        FabricClientBusSubscriber.registerEvents();
        NetworkUtils.registerClient(CLIENT);
        PluginManager.getInstance().registerClientEvent();
    }
}
