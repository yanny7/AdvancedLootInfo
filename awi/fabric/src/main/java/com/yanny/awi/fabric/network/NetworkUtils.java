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
        PayloadTypeRegistry.clientboundPlay().register(WorldgenDataChunkMessage.TYPE, WorldgenDataChunkMessage.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StartMessage.TYPE, StartMessage.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DoneMessage.TYPE, DoneMessage.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(RequestWorldgenDataMessage.TYPE, RequestWorldgenDataMessage.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestWorldgenDataMessage.TYPE, server::onStartSendingWorldgenData);
    }
}
