package com.yanny.awi.fabric.network;

import com.yanny.awi.network.AbstractServer;
import com.yanny.awi.network.DoneMessage;
import com.yanny.awi.network.StartMessage;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
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

    }

    @Override
    protected void sendWorldgenDataChunkMessage(ServerPlayer serverPlayer, WorldgenDataChunkMessage message) {

    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {

    }
}
