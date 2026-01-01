package com.yanny.ali;

import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.Server;
import net.fabricmc.api.ModInitializer;

public class CommonAliMod implements ModInitializer {
    public static final AbstractServer SERVER;

    static {
        SERVER = new Server();
    }

    @Override
    public void onInitialize() {
        FabricBusSubscriber.registerEvents();
        PluginManager.registerCommonEvent();
    }
}
