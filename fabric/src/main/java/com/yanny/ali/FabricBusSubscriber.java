package com.yanny.ali;

import com.yanny.ali.manager.PluginManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class FabricBusSubscriber {
    public static void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(FabricBusSubscriber::onServerStarting);
        ServerPlayConnectionEvents.JOIN.register(FabricBusSubscriber::onPlayerLogIn);
    }

    private static void onServerStarting(MinecraftServer server) {
        PluginManager.registerServerEvent();
        CommonAliMod.INFO_PROPAGATOR.server().readLootTables(server.reloadableRegistries(), server.overworld());
    }

    private static void onPlayerLogIn(ServerGamePacketListenerImpl event, PacketSender sender, MinecraftServer server) {
        CommonAliMod.INFO_PROPAGATOR.server().syncLootTables(event.player);
    }
}
