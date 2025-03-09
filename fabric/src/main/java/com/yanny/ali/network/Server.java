package com.yanny.ali.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class Server extends AbstractServer {
    @Override
    protected void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message) {
        ServerPlayNetworking.send(serverPlayer, message);
    }

    @Override
    protected void sendSyncMessage(ServerPlayer serverPlayer, InfoSyncLootTableMessage message) {
        FriendlyByteBuf buf = PacketByteBufs.create();

        message.write(buf);
        ServerPlayNetworking.send(serverPlayer, message);
    }
}
