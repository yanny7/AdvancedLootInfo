package com.yanny.ali;

import com.yanny.ali.manager.PluginManager;
import net.fabricmc.api.ClientModInitializer;

public class ClientAliMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PluginManager.registerClientEvent();
    }
}
