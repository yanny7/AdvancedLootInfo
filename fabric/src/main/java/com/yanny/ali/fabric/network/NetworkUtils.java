package com.yanny.ali.fabric.network;

import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class NetworkUtils {
    public static void registerClient() {
        Client client = new Client();

        PayloadTypeRegistry.clientboundPlay().register(LootDataChunkMessage.TYPE, LootDataChunkMessage.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StartMessage.TYPE, StartMessage.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DoneMessage.TYPE, DoneMessage.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(LootDataChunkMessage.TYPE, client::onLootDataChunk);
        ClientPlayNetworking.registerGlobalReceiver(StartMessage.TYPE, client::onStart);
        ClientPlayNetworking.registerGlobalReceiver(DoneMessage.TYPE, client::onDone);
    }

    public static void registerCommon() {
        PayloadTypeRegistry.serverboundPlay().register(StartMessage.TYPE, StartMessage.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(StartMessage.TYPE, (s, c) -> {});
    }
}
