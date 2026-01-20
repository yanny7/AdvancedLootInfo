package com.yanny.ali.neoforge.network;

import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class Server extends AbstractServer {
    @Override
    protected void sendStartMessage(ServerPlayer serverPlayer, StartMessage message) {
        if (serverPlayer.connection.hasChannel(StartMessage.TYPE)) {
            PacketDistributor.sendToPlayer(serverPlayer, message);
        }
    }

    @Override
    protected void sendLootDataChunkMessage(ServerPlayer serverPlayer, LootDataChunkMessage message) {
        if (serverPlayer.connection.hasChannel(LootDataChunkMessage.TYPE)) {
            PacketDistributor.sendToPlayer(serverPlayer, message);
        }
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        if (serverPlayer.connection.hasChannel(DoneMessage.TYPE)) {
            PacketDistributor.sendToPlayer(serverPlayer, message);
        }
    }
}
