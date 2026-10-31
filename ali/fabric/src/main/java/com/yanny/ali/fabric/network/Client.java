package com.yanny.ali.fabric.network;

import com.yanny.ali.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class Client extends AbstractClient {
    public void onLootDataChunk(LootDataChunkMessage message, ClientPlayNetworking.Context context) {
        super.onLootDataChunk(message);
    }

    public void onStart(StartMessage message, ClientPlayNetworking.Context context) {
        super.onStart(message);
    }

    public void onDone(DoneMessage message, ClientPlayNetworking.Context context) {
        super.onDone(message);
    }

    @Override
    public boolean canRequestData() {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();

        return listener != null && listener.getConnection().isConnected() && ClientPlayNetworking.canSend(RequestLootDataMessage.TYPE);
    }

    @Override
    public void sendLootDataToPlayer(RequestLootDataMessage message) {
        if (canRequestData()) {
            ClientPlayNetworking.send(message);
        }
    }
}
