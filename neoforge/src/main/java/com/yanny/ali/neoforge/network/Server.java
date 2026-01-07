package com.yanny.ali.neoforge.network;

import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class Server extends AbstractServer {
    @Override
    protected void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message) {
        PacketDistributor.sendToPlayer(serverPlayer, message);
    }

    @Override
    protected void sendSyncLootTableMessage(ServerPlayer serverPlayer, LootDataChunkMessage message) {
        PacketDistributor.sendToPlayer(serverPlayer, message);
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        PacketDistributor.sendToPlayer(serverPlayer, message);
    }
}
