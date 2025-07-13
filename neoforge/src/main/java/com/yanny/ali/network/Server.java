package com.yanny.ali.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.simple.SimpleChannel;

public class Server extends AbstractServer {
    private final SimpleChannel channel;

    public Server(SimpleChannel channel) {
        this.channel = channel;
    }

    @Override
    protected void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message) {
        channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
    }

    @Override
    protected void sendSyncMessage(ServerPlayer serverPlayer, SyncLootTableMessage message) {
        channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
    }
}
