package com.yanny.ali.network;

import com.yanny.ali.manager.PluginManager;

public abstract class AbstractClient {
    public static AbstractClient INSTANCE;

    protected AbstractClient() {
        INSTANCE = this;
    }

    protected void onLootDataChunk(LootDataChunkMessage msg) {
        PluginManager.getInstance().clientRegistry.addChunkData(msg.index(), msg.data());
    }

    protected void onStart(StartMessage msg) {
        PluginManager.getInstance().clientRegistry.startLootData(msg.totalMessages);
    }

    protected void onDone(DoneMessage ignoredMsg) {
        PluginManager.getInstance().clientRegistry.doneLootData();
    }

    public abstract void sendLootDataToPlayer(RequestLootDataMessage message);
}
