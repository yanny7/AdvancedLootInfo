package com.yanny.ali.fabric;

import com.yanny.ali.fabric.network.NetworkUtils;
import com.yanny.ali.manager.PluginManager;
import net.fabricmc.api.ClientModInitializer;

public class ClientAliMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NetworkUtils.registerClient();
        PluginManager.registerClientEvent();
    }
}
