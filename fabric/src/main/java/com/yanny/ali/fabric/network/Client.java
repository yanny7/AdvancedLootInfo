package com.yanny.ali.fabric.network;

import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class Client extends AbstractClient {
    public void onLootInfo(LootDataChunkMessage message, ClientPlayNetworking.Context context) {
        super.onLootInfo(message);
    }

    public void onClear(ClearMessage message, ClientPlayNetworking.Context context) {
        super.onClear(message);
    }

    public void onDone(DoneMessage message, ClientPlayNetworking.Context context) {
        super.onDone(message);
    }
}
