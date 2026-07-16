package com.yanny.awi.fabric.network;

import com.yanny.awi.network.DoneMessage;
import com.yanny.awi.network.RequestWorldgenDataMessage;
import com.yanny.awi.network.StartMessage;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class NetworkUtils {
    public static void registerClient(Client client) {
        ClientPlayNetworking.registerGlobalReceiver(WorldgenDataChunkMessage.TYPE, client::onWorldgenDataChunk);
        ClientPlayNetworking.registerGlobalReceiver(StartMessage.TYPE, client::onStart);
        ClientPlayNetworking.registerGlobalReceiver(DoneMessage.TYPE, client::onDone);
    }

    public static void registerCommon(Server server) {
        PayloadTypeRegistry.playC2S().register(RequestWorldgenDataMessage.TYPE, RequestWorldgenDataMessage.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestWorldgenDataMessage.TYPE, server::onStartSendingWorldgenData);

        PayloadTypeRegistry.playS2C().register(WorldgenDataChunkMessage.TYPE, WorldgenDataChunkMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(StartMessage.TYPE, StartMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(DoneMessage.TYPE, DoneMessage.CODEC);
    }
}
