package com.yanny.ali.fabric;

import com.yanny.ali.fabric.network.NetworkUtils;
import com.yanny.ali.manager.PluginManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class FabricClientBusSubscriber {
    public static void registerEvents() {
        ClientPlayConnectionEvents.JOIN.register(FabricClientBusSubscriber::onConnect);
        ClientPlayConnectionEvents.DISCONNECT.register(FabricClientBusSubscriber::onDisconnect);
    }

    private static void onConnect(ClientPacketListener clientPacketListener, PacketSender packetSender, Minecraft minecraft) {
        PluginManager.getInstance().clientRegistry.loggingIn(ClientPlayNetworking.canSend(NetworkUtils.REQUEST_LOOT_DATA_ID));
    }

    private static void onDisconnect(ClientPacketListener clientPacketListener, Minecraft minecraft) {
        PluginManager.getInstance().clientRegistry.loggingOut();
    }
}
