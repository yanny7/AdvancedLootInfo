package com.yanny.awi.forge.network;

import com.yanny.awi.network.AbstractServer;
import com.yanny.awi.network.DoneMessage;
import com.yanny.awi.network.RequestWorldgenDataMessage;
import com.yanny.awi.network.StartMessage;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class Server extends AbstractServer {
    private final SimpleChannel channel;

    public Server(SimpleChannel channel) {
        this.channel = channel;
    }

    public void onStartSendingWorldgenData(@SuppressWarnings("unused") RequestWorldgenDataMessage ignoredMessage, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isServer() && context.getSender() != null) {
            context.enqueueWork(() -> syncLootTables(context.getSender()));
        }

        context.setPacketHandled(true);
    }

    @Override
    protected void sendStartMessage(ServerPlayer serverPlayer, StartMessage message) {
        if (channel.isRemotePresent(serverPlayer.connection.connection)) {
            channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
        }
    }

    @Override
    protected void sendWorldgenDataChunkMessage(ServerPlayer serverPlayer, WorldgenDataChunkMessage message) {
        if (channel.isRemotePresent(serverPlayer.connection.connection)) {
            channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
        }
    }

    @Override
    protected void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message) {
        if (channel.isRemotePresent(serverPlayer.connection.connection)) {
            channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
        }
    }
}
