package com.yanny.ali.fabric.network;

import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class Server extends AbstractServer {
    @Override
    protected void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message) {
        ServerPlayNetworking.send(serverPlayer, message);
    }

    @Override
    protected void sendSyncLootTableMessage(ServerPlayer serverPlayer, LootDataChunkMessage message) {
        ServerPlayNetworking.send(serverPlayer, message);
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        ServerPlayNetworking.send(serverPlayer, message);
    }
}
