package com.yanny.awi.fabric;

import com.yanny.awi.fabric.network.Server;
import com.yanny.awi.network.AbstractServer;
import net.fabricmc.api.ModInitializer;

public class CommonAliMod implements ModInitializer {
    public static final AbstractServer SERVER;

    static {
        SERVER = new Server();
    }

    @Override
    public void onInitialize() {
        FabricCommonBusSubscriber.registerEvents();
    }
}
