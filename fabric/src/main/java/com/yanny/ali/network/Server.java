package com.yanny.ali.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class Server extends AbstractServer {
    @Override
    protected void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message) {
        ServerPlayNetworking.send(serverPlayer, message);
    }

    @Override
    protected void sendSyncLootTableMessage(ServerPlayer serverPlayer, SyncLootTableMessage message) {
        ServerPlayNetworking.send(serverPlayer, message);
    }

    @Override
    protected void sendSyncTradeMessage(ServerPlayer serverPlayer, SyncTradeMessage message) {
        ServerPlayNetworking.send(serverPlayer, message);
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        ServerPlayNetworking.send(serverPlayer, message);
    }
}
