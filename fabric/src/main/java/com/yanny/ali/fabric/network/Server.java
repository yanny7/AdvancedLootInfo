package com.yanny.ali.fabric.network;

import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class Server extends AbstractServer {
    @Override
    protected void sendStartMessage(ServerPlayer serverPlayer, StartMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, NetworkUtils.START_LOOT_INFO_ID)) {
            ServerPlayNetworking.send(serverPlayer, message);
        }
    }

    @Override
    protected void sendLootDataChunkMessage(ServerPlayer serverPlayer, LootDataChunkMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, NetworkUtils.LOOT_DATA_CHUNK_ID)) {
            ServerPlayNetworking.send(serverPlayer, message);
        }
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, NetworkUtils.DONE_LOOT_INFO_ID)) {
            ServerPlayNetworking.send(serverPlayer, message);
        }
    }
}
