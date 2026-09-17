package com.yanny.awi.fabric;

import com.yanny.awi.manager.PluginManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class FabricCommonBusSubscriber {
    private static boolean serverLoaded = false;

    public static void registerEvents() {
        ServerWorldEvents.LOAD.register(FabricCommonBusSubscriber::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(FabricCommonBusSubscriber::onServerStopping);
    }

    private static void onServerStarting(MinecraftServer server, ServerLevel world) {
        if (!serverLoaded) { // to be safe, handle only once for world loading (should be called only once for overworld, but who knows?)
            PluginManager.getInstance().registerServerEvent(world);
            CommonAwiMod.SERVER.readWorldgenInfo(server.overworld());
            serverLoaded = true;
        }
    }

    private static void onServerStopping(MinecraftServer server) {
        serverLoaded = false;
        PluginManager.getInstance().deregisterServerEvent();
    }
}
