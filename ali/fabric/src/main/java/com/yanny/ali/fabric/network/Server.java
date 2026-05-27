package com.yanny.ali.fabric.network;

import com.yanny.ali.network.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class Server extends AbstractServer {
    protected void onStartSendingLootData(RequestLootDataMessage message, ServerPlayNetworking.Context context) {
        context.server().execute(() -> syncLootTables(context.player()));
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
