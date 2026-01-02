package com.yanny.ali.fabric.network;

import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class Server extends AbstractServer {
    @Override
    protected void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message) {
        FriendlyByteBuf buf = PacketByteBufs.create();

        message.encode(buf);
        ServerPlayNetworking.send(serverPlayer, NetworkUtils.CLEAR_LOOT_INFO_ID, buf);
    }

    @Override
    protected void sendSyncLootTableMessage(ServerPlayer serverPlayer, LootDataChunkMessage message) {
        FriendlyByteBuf buf = PacketByteBufs.create();

        message.encode(buf);
        ServerPlayNetworking.send(serverPlayer, NetworkUtils.NEW_LOOT_INFO_ID, buf);
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        FriendlyByteBuf buf = PacketByteBufs.create();

        message.encode(buf);
        ServerPlayNetworking.send(serverPlayer, NetworkUtils.DONE_LOOT_INFO_ID, buf);
    }
}
