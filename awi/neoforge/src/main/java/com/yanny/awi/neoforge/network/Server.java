package com.yanny.awi.neoforge.network;

import com.yanny.awi.network.AbstractServer;
import com.yanny.awi.network.DoneMessage;
import com.yanny.awi.network.RequestWorldgenDataMessage;
import com.yanny.awi.network.StartMessage;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class Server extends AbstractServer {
    public void onStartSendingWorldgenData(@SuppressWarnings("unused") RequestWorldgenDataMessage ignoredMessage, IPayloadContext context) {
        context.enqueueWork(() -> syncLootTables(context.player()));
    }

    @Override
    protected void sendStartMessage(ServerPlayer serverPlayer, StartMessage message) {
        if (serverPlayer.connection.hasChannel(StartMessage.TYPE)) {
            PacketDistributor.sendToPlayer(serverPlayer, message);
        }
    }

    @Override
    protected void sendWorldgenDataChunkMessage(ServerPlayer serverPlayer, WorldgenDataChunkMessage message) {
        if (serverPlayer.connection.hasChannel(WorldgenDataChunkMessage.TYPE)) {
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
