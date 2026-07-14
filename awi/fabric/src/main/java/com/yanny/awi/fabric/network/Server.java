package com.yanny.awi.fabric.network;

import com.yanny.awi.network.AbstractServer;
import com.yanny.awi.network.DoneMessage;
import com.yanny.awi.network.StartMessage;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class Server extends AbstractServer {
    protected void onStartSendingWorldgenData(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl ignoredHandler,
                                              FriendlyByteBuf ignoredBuf, PacketSender ignoredResponseSender) {
        server.execute(() -> syncLootTables(player));
    }

    @Override
    protected void sendStartMessage(ServerPlayer serverPlayer, StartMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, NetworkUtils.START_WORLDGEN_INFO_ID)) {
            FriendlyByteBuf buf = PacketByteBufs.create();

            message.encode(buf);
            ServerPlayNetworking.send(serverPlayer, NetworkUtils.START_WORLDGEN_INFO_ID, buf);
        }
    }

    @Override
    protected void sendWorldgenDataChunkMessage(ServerPlayer serverPlayer, WorldgenDataChunkMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, NetworkUtils.WORLDGEN_DATA_CHUNK_ID)) {
            FriendlyByteBuf buf = PacketByteBufs.create();

            message.encode(buf);
            ServerPlayNetworking.send(serverPlayer, NetworkUtils.WORLDGEN_DATA_CHUNK_ID, buf);
        }
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        if (ServerPlayNetworking.canSend(serverPlayer, NetworkUtils.DONE_WORLDGEN_INFO_ID)) {
            FriendlyByteBuf buf = PacketByteBufs.create();

            message.encode(buf);
            ServerPlayNetworking.send(serverPlayer, NetworkUtils.DONE_WORLDGEN_INFO_ID, buf);
        }
    }
}
