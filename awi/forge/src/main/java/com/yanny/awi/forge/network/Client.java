package com.yanny.awi.forge.network;

import com.yanny.awi.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class Client extends AbstractClient {
    private final SimpleChannel channel;

    public Client(SimpleChannel channel) {
        this.channel = channel;
    }

    public void onWorldgenDataChunk(WorldgenDataChunkMessage msg, CustomPayloadEvent.Context contextSupplier) {
        if (contextSupplier.isClientSide()) {
            super.onLootDataChunk(msg);
        }

        contextSupplier.setPacketHandled(true);
    }

    public void onStart(StartMessage msg, CustomPayloadEvent.Context contextSupplier) {
        if (contextSupplier.isClientSide()) {
            super.onStart(msg);
        }

        contextSupplier.setPacketHandled(true);
    }

    public void onDone(DoneMessage msg, CustomPayloadEvent.Context contextSupplier) {
        if (contextSupplier.isClientSide()) {
            super.onDone(msg);
        }

        contextSupplier.setPacketHandled(true);
    }

    @Override
    public void sendLootDataToPlayer(RequestWorldgenDataMessage message) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();

        if (listener != null && channel.isRemotePresent(listener.getConnection())) {
            channel.send(message, PacketDistributor.SERVER.noArg());
        }
    }
}
