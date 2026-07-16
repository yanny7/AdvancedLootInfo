package com.yanny.awi.fabric.network;

import com.yanny.awi.network.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class Server extends AbstractServer {
    protected void onStartSendingWorldgenData(RequestWorldgenDataMessage message, ServerPlayNetworking.Context context) {
        context.server().execute(() -> syncLootTables(context.player()));
    }

    @Override
    protected void sendStartMessage(ServerPlayer serverPlayer, StartMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, StartMessage.TYPE)) {
            ServerPlayNetworking.send(serverPlayer, message);
        }
    }

    @Override
    protected void sendWorldgenDataChunkMessage(ServerPlayer serverPlayer, WorldgenDataChunkMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, WorldgenDataChunkMessage.TYPE)) {
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
