package com.yanny.ali.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class Client extends AbstractClient {
    public void onLootInfo(InfoSyncLootTableMessage message, ClientPlayNetworking.Context context) {
        super.onLootInfo(message, context.player().registryAccess());
    }

    public void onClear(ClearMessage message, ClientPlayNetworking.Context context) {
        super.onClear(message);
    }
}
