package com.yanny.ali.fabric;

import com.yanny.ali.fabric.network.NetworkUtils;
import com.yanny.ali.fabric.network.Server;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import net.fabricmc.api.ModInitializer;

public class CommonAliMod implements ModInitializer {
    public static final AbstractServer SERVER;

    static {
        SERVER = new Server();
    }

    @Override
    public void onInitialize() {
        FabricCommonBusSubscriber.registerEvents();
        NetworkUtils.registerCommon();
        PluginManager.registerCommonEvent();
    }
}
