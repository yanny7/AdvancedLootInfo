package com.yanny.awi.fabric;

import com.yanny.awi.fabric.network.NetworkUtils;
import com.yanny.awi.fabric.network.Server;
import com.yanny.awi.manager.PluginManager;
import net.fabricmc.api.ModInitializer;

public class CommonAliMod implements ModInitializer {
    public static final Server SERVER = new Server();

    @Override
    public void onInitialize() {
        FabricCommonBusSubscriber.registerEvents();
        NetworkUtils.registerCommon(SERVER);
        PluginManager.getInstance().registerCommonEvent();
    }
}
