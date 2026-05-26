package com.yanny.ali.forge.network;

import com.yanny.ali.network.*;
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

    public void onLootDataChunk(LootDataChunkMessage msg, CustomPayloadEvent.Context contextSupplier) {
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

    protected void onDone(DoneMessage msg, CustomPayloadEvent.Context contextSupplier) {
        if (contextSupplier.isClientSide()) {
            super.onDone(msg);
        }

        contextSupplier.setPacketHandled(true);
    }

    @Override
    public void sendLootDataToPlayer(RequestLootDataMessage message) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();

        if (listener != null && channel.isRemotePresent(listener.getConnection())) {
            channel.send(message, PacketDistributor.SERVER.noArg());
        }
    }
}
