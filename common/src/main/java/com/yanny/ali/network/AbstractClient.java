package com.yanny.ali.network;

import com.yanny.ali.manager.PluginManager;

public abstract class AbstractClient {
    protected void onLootInfo(InfoSyncLootTableMessage msg) {
        PluginManager.CLIENT_REGISTRY.addLootTable(msg.location, msg.lootTable);
    }

    protected void onClear(ClearMessage msg) {
        PluginManager.CLIENT_REGISTRY.clearLootTables();
    }
}
