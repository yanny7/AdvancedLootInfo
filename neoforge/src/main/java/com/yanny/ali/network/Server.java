package com.yanny.ali.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class Server extends AbstractServer {
    @Override
    protected void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message) {
        PacketDistributor.PLAYER.with(serverPlayer).send(message);
    }

    @Override
    protected void sendSyncLootTableMessage(ServerPlayer serverPlayer, SyncLootTableMessage message) {
        PacketDistributor.PLAYER.with(serverPlayer).send(message);
    }

    @Override
    protected void sendSyncTradeMessage(ServerPlayer serverPlayer, SyncTradeMessage message) {
        PacketDistributor.PLAYER.with(serverPlayer).send(message);
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        PacketDistributor.PLAYER.with(serverPlayer).send(message);
    }
}
