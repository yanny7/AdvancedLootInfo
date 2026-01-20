package com.yanny.ali.network;

import com.yanny.ali.manager.PluginManager;

public abstract class AbstractClient {
    protected void onLootDataChunk(LootDataChunkMessage msg) {
        PluginManager.CLIENT_REGISTRY.addChunkData(msg.index(), msg.data());
    }

    protected void onStart(StartMessage msg) {
        PluginManager.CLIENT_REGISTRY.startLootData(msg.totalMessages);
    }

    protected void onDone(DoneMessage ignoredMsg) {
        PluginManager.CLIENT_REGISTRY.doneLootData();
    }
}
