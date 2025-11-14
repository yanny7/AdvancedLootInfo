package com.yanny.ali.network;

import com.yanny.ali.manager.PluginManager;

public abstract class AbstractClient {
    protected void onLootInfo(LootDataChunkMessage msg) {
        PluginManager.CLIENT_REGISTRY.addChunkData(msg.index(), msg.data());
    }

    protected void onClear(ClearMessage msg) {
        PluginManager.CLIENT_REGISTRY.startLootData(msg.totalMessages);
    }

    protected void onDone(DoneMessage msg) {
        PluginManager.CLIENT_REGISTRY.doneLootData();
    }
}
