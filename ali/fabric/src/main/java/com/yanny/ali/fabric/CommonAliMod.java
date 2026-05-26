package com.yanny.ali.fabric;

import com.yanny.ali.fabric.network.NetworkUtils;
import com.yanny.ali.fabric.network.Server;
import com.yanny.ali.manager.PluginManager;
import net.fabricmc.api.ModInitializer;

public class CommonAliMod implements ModInitializer {
    public static final Server SERVER = new Server();

    @Override
    public void onInitialize() {
        FabricCommonBusSubscriber.registerEvents(SERVER);
        NetworkUtils.registerCommon(SERVER);
        PluginManager.getInstance().registerCommonEvent();
    }
}
