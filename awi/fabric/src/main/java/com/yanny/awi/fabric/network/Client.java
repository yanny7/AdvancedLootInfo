package com.yanny.awi.fabric.network;

import com.yanny.awi.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class Client extends AbstractClient {
    public void onWorldgenDataChunk(WorldgenDataChunkMessage message, ClientPlayNetworking.Context context) {
        super.onLootDataChunk(message);
    }

    public void onStart(StartMessage message, ClientPlayNetworking.Context context) {
        super.onStart(message);
    }

    public void onDone(DoneMessage message, ClientPlayNetworking.Context context) {
        super.onDone(message);
    }

    @Override
    public void sendLootDataToPlayer(RequestWorldgenDataMessage message) {
        if (ClientPlayNetworking.canSend(RequestWorldgenDataMessage.TYPE)) {
            ClientPlayNetworking.send(message);
        }
    }
}
