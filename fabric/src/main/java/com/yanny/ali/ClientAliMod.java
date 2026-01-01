package com.yanny.ali;

import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.NetworkUtils;
import net.fabricmc.api.ClientModInitializer;

public class ClientAliMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NetworkUtils.registerClient();
        PluginManager.registerClientEvent();
    }
}
