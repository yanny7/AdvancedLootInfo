package com.yanny.ali.fabric.network;

import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

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
}
