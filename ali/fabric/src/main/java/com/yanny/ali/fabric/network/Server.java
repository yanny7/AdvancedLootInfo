package com.yanny.ali.fabric.network;

import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class Server extends AbstractServer {
    @Override
    protected void sendStartMessage(ServerPlayer serverPlayer, StartMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, NetworkUtils.START_LOOT_INFO_ID)) {
            FriendlyByteBuf buf = PacketByteBufs.create();

            message.encode(buf);
            ServerPlayNetworking.send(serverPlayer, NetworkUtils.START_LOOT_INFO_ID, buf);
        }
    }

    @Override
    protected void sendLootDataChunkMessage(ServerPlayer serverPlayer, LootDataChunkMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, NetworkUtils.LOOT_DATA_CHUNK_ID)) {
            FriendlyByteBuf buf = PacketByteBufs.create();

            message.encode(buf);
            ServerPlayNetworking.send(serverPlayer, NetworkUtils.LOOT_DATA_CHUNK_ID, buf);
        }
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, NetworkUtils.DONE_LOOT_INFO_ID)) {
            FriendlyByteBuf buf = PacketByteBufs.create();

            message.encode(buf);
            ServerPlayNetworking.send(serverPlayer, NetworkUtils.DONE_LOOT_INFO_ID, buf);
        }
    }
}
