package com.yanny.ali.forge.network;

import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class Server extends AbstractServer {
    private final SimpleChannel channel;

    public Server(SimpleChannel channel) {
        this.channel = channel;
    }

    @Override
    protected void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message) {
        channel.send(message, PacketDistributor.PLAYER.with(serverPlayer));
    }

    @Override
    protected void sendSyncLootTableMessage(ServerPlayer serverPlayer, LootDataChunkMessage message) {
        channel.send(message, PacketDistributor.PLAYER.with(serverPlayer));
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        channel.send(message, PacketDistributor.PLAYER.with(serverPlayer));
    }
}
