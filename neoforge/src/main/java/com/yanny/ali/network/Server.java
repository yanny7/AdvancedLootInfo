package com.yanny.ali.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class Server extends AbstractServer {
    @Override
    protected void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message) {
        PacketDistributor.sendToPlayer(serverPlayer, message);
    }

    @Override
    protected void sendSyncMessage(ServerPlayer serverPlayer, InfoSyncLootTableMessage message) {
        PacketDistributor.sendToPlayer(serverPlayer, message);
    }
}
