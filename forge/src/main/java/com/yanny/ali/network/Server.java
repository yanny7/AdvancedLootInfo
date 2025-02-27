package com.yanny.ali.network;

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
    protected void sendSyncMessage(ServerPlayer serverPlayer, InfoSyncLootTableMessage message) {
        channel.send(message, PacketDistributor.PLAYER.with(serverPlayer));
    }
}
