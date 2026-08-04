package com.yanny.awi.forge.network;

import com.yanny.awi.network.AbstractServer;
import com.yanny.awi.network.DoneMessage;
import com.yanny.awi.network.RequestWorldgenDataMessage;
import com.yanny.awi.network.StartMessage;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class Server extends AbstractServer {
    private final SimpleChannel channel;

    public Server(SimpleChannel channel) {
        this.channel = channel;
    }

    public void onStartSendingWorldgenData(@SuppressWarnings("unused") RequestWorldgenDataMessage ignoredMessage, CustomPayloadEvent.Context contextSupplier) {
        if (contextSupplier.isServerSide() && contextSupplier.getSender() != null) {
            contextSupplier.enqueueWork(() -> syncLootTables(contextSupplier.getSender()));
        }

        contextSupplier.setPacketHandled(true);
    }

    @Override
    protected void sendStartMessage(ServerPlayer serverPlayer, StartMessage message) {
        if (channel.isRemotePresent(serverPlayer.connection.getConnection())) {
            channel.send(message, PacketDistributor.PLAYER.with(serverPlayer));
        }
    }

    @Override
    protected void sendWorldgenDataChunkMessage(ServerPlayer serverPlayer, WorldgenDataChunkMessage message) {
        if (channel.isRemotePresent(serverPlayer.connection.getConnection())) {
            channel.send(message, PacketDistributor.PLAYER.with(serverPlayer));
        }
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        if (channel.isRemotePresent(serverPlayer.connection.getConnection())) {
            channel.send(message, PacketDistributor.PLAYER.with(serverPlayer));
        }
    }
}
