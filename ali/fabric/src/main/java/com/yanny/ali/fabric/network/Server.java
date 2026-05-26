package com.yanny.ali.fabric.network;

import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class Server extends AbstractServer {
    protected void onStartSendingLootData(MinecraftServer ignoredServer, ServerPlayer player, ServerGamePacketListenerImpl ignoredHandler,
                                          FriendlyByteBuf ignoredBuf, PacketSender ignoredResponseSender) {
        syncLootTables(player);
    }

    @Override
    protected void sendStartMessage(ServerPlayer serverPlayer, StartMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, StartMessage.TYPE)) {
            ServerPlayNetworking.send(serverPlayer, message);
        }
    }

    @Override
    protected void sendLootDataChunkMessage(ServerPlayer serverPlayer, LootDataChunkMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, LootDataChunkMessage.TYPE)) {
            ServerPlayNetworking.send(serverPlayer, message);
        }
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, DoneMessage.TYPE)) {
            ServerPlayNetworking.send(serverPlayer, message);
        }
    }
}
