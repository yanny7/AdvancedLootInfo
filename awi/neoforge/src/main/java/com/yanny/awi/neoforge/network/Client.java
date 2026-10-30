package com.yanny.awi.neoforge.network;

import com.yanny.awi.network.AbstractClient;
import com.yanny.awi.network.DoneMessage;
import com.yanny.awi.network.RequestWorldgenDataMessage;
import com.yanny.awi.network.StartMessage;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class Client extends AbstractClient {
    public void onWorldgenDataChunk(WorldgenDataChunkMessage msg, @SuppressWarnings("unused") IPayloadContext context) {
        super.onWorldgenDataChunk(msg);
    }

    public void onStart(StartMessage msg, @SuppressWarnings("unused") IPayloadContext context) {
        super.onStart(msg);
    }

    public void onDone(DoneMessage msg, @SuppressWarnings("unused") IPayloadContext context) {
        super.onDone(msg);
    }

    @Override
    public boolean canRequestData() {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();

        return listener != null && listener.getConnection().isConnected() && listener.hasChannel(RequestWorldgenDataMessage.TYPE);
    }

    @Override
    public void sendWorldgenDataToPlayer(RequestWorldgenDataMessage message) {
        if (canRequestData()) {
            PacketDistributor.sendToServer(message);
        }
    }
}
