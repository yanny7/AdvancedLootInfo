package com.yanny.ali.fabric;

import com.yanny.ali.manager.PluginManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;

public class FabricClientBusSubscriber {
    public static void registerEvents() {
        ClientLoginConnectionEvents.DISCONNECT.register(FabricClientBusSubscriber::onDisconnect);
    }

    private static void onDisconnect(ClientHandshakePacketListenerImpl clientHandshakePacketListener, Minecraft minecraft) {
        PluginManager.CLIENT_REGISTRY.logOut();
    }
}
