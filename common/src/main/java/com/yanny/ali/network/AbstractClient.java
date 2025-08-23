package com.yanny.ali.network;

import com.yanny.ali.manager.PluginManager;

public abstract class AbstractClient {
    protected void onLootInfo(SyncLootTableMessage msg) {
        PluginManager.CLIENT_REGISTRY.addLootData(msg.location, msg.node, msg.items);
    }

    protected void onTradeInfo(SyncTradeMessage msg) {
        PluginManager.CLIENT_REGISTRY.addTradeData(msg.location, msg.node, msg.inputs, msg.outputs);
    }

    protected void onClear(ClearMessage msg) {
        PluginManager.CLIENT_REGISTRY.clearLootData();
    }

    protected void onDone(DoneMessage msg) {
        PluginManager.CLIENT_REGISTRY.doneLootData();
    }
}
