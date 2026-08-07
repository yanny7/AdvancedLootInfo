package com.yanny.awi.neoforge.network;

import com.yanny.awi.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
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
    public void sendWorldgenDataToPlayer(RequestWorldgenDataMessage message) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();

        if (listener != null && listener.hasChannel(RequestWorldgenDataMessage.TYPE)) {
            ClientPacketDistributor.sendToServer(message);
        }
    }
}
