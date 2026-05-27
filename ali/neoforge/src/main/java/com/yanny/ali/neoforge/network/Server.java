package com.yanny.ali.neoforge.network;

import com.yanny.ali.network.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class Server extends AbstractServer {
    public void onStartSendingLootData(RequestLootDataMessage ignoredMessage, IPayloadContext contextSupplier) {
        contextSupplier.enqueueWork(() -> syncLootTables(contextSupplier.player()));
    }

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
