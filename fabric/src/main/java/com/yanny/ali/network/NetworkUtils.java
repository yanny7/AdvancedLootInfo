package com.yanny.ali.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class NetworkUtils {
    public static void registerClient() {
        Client client = new Client();

        PayloadTypeRegistry.playS2C().register(LootDataChunkMessage.TYPE, LootDataChunkMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(ClearMessage.TYPE, ClearMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(DoneMessage.TYPE, DoneMessage.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(LootDataChunkMessage.TYPE, client::onLootInfo);
        ClientPlayNetworking.registerGlobalReceiver(ClearMessage.TYPE, client::onClear);
        ClientPlayNetworking.registerGlobalReceiver(DoneMessage.TYPE, client::onDone);
    }
}
