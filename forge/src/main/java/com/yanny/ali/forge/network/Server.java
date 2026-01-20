package com.yanny.ali.forge.network;

import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class Server extends AbstractServer {
    private final SimpleChannel channel;

    public Server(SimpleChannel channel) {
        this.channel = channel;
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
