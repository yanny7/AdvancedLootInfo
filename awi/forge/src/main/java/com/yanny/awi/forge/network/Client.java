package com.yanny.awi.forge.network;

import com.yanny.awi.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class Client extends AbstractClient {
    private final SimpleChannel channel;

    public Client(SimpleChannel channel) {
        this.channel = channel;
    }

    public void onWorldgenDataChunk(WorldgenDataChunkMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection().getReceptionSide().isClient()) {
            super.onWorldgenDataChunk(msg);
        }

        contextSupplier.get().setPacketHandled(true);
    }

    public void onStart(StartMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection().getReceptionSide().isClient()) {
            super.onStart(msg);
        }

        contextSupplier.get().setPacketHandled(true);
    }

    public void onDone(DoneMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection().getReceptionSide().isClient()) {
            super.onDone(msg);
        }

        contextSupplier.get().setPacketHandled(true);
    }

    @Override
    public void sendWorldgenDataToPlayer(RequestWorldgenDataMessage message) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();

        if (listener != null && channel.isRemotePresent(listener.getConnection())) {
            channel.sendToServer(message);
        }
    }
}
