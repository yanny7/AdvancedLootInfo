package com.yanny.ali.fabric;

import com.yanny.ali.fabric.network.Client;
import com.yanny.ali.fabric.network.NetworkUtils;
import com.yanny.ali.manager.PluginManager;
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
