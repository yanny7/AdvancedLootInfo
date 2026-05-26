package com.yanny.ali.forge.network;

import com.yanny.ali.network.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class Server extends AbstractServer {
    private final SimpleChannel channel;

    public Server(SimpleChannel channel) {
        this.channel = channel;
    }

    public void onStartSendingLootData(RequestLootDataMessage ignoredMessage, CustomPayloadEvent.Context contextSupplier) {
        if (contextSupplier.isServerSide() && contextSupplier.getSender() != null) {
            syncLootTables(contextSupplier.getSender());
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
    protected void sendLootDataChunkMessage(ServerPlayer serverPlayer, LootDataChunkMessage message) {
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
