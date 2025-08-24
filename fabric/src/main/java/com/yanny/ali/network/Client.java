package com.yanny.ali.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class Client extends AbstractClient {
    public void onLootInfo(SyncLootTableMessage message, ClientPlayNetworking.Context context) {
        super.onLootInfo(message);
    }

    public void onTradeInfo(SyncTradeMessage message, ClientPlayNetworking.Context context) {
        super.onTradeInfo(message);
    }

    public void onClear(ClearMessage message, ClientPlayNetworking.Context context) {
        super.onClear(message);
    }

    public void onDone(DoneMessage message, ClientPlayNetworking.Context context) {
        super.onDone(message);
    }
}
