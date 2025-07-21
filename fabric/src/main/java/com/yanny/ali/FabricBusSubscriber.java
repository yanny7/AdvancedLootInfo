package com.yanny.ali;

import com.yanny.ali.manager.PluginManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class FabricBusSubscriber {
    private static boolean serverLoaded = false;

    public static void registerEvents() {
        ServerWorldEvents.LOAD.register(FabricBusSubscriber::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(FabricBusSubscriber::onServerStopping);
        ServerPlayConnectionEvents.JOIN.register(FabricBusSubscriber::onPlayerLogIn);
    }

    private static void onServerStarting(MinecraftServer server, ServerLevel world) {
        if (!serverLoaded) { // to be safe, handle only once for world loading (should be called only once for overworld, but who knows?)
            PluginManager.registerServerEvent();
            CommonAliMod.INFO_PROPAGATOR.server().readLootTables(server.getLootData(), server.overworld());
            serverLoaded = true;
        }
    }

    private static void onServerStopping(MinecraftServer server) {
        serverLoaded = false;
    }

    private static void onPlayerLogIn(ServerGamePacketListenerImpl event, PacketSender sender, MinecraftServer server) {
        CommonAliMod.INFO_PROPAGATOR.server().syncLootTables(event.player);
    }
}
