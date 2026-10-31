package com.yanny.ali.neoforge.network;

import com.yanny.ali.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class Client extends AbstractClient {
    public void onLootDataChunk(LootDataChunkMessage msg, IPayloadContext contextSupplier) {
        super.onLootDataChunk(msg);
    }

    public void onStart(StartMessage msg, IPayloadContext contextSupplier) {
        super.onStart(msg);
    }

    public void onDone(DoneMessage msg, IPayloadContext contextSupplier) {
        super.onDone(msg);
    }

    @Override
    public boolean canRequestData() {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();

        return listener != null && listener.getConnection().isConnected() && listener.hasChannel(StartMessage.TYPE);
    }

    @Override
    public void sendLootDataToPlayer(RequestLootDataMessage message) {
        if (canRequestData()) {
            ClientPacketDistributor.sendToServer(message);
        }
    }
}
