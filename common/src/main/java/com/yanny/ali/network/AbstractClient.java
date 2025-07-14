package com.yanny.ali.network;

import com.yanny.ali.manager.PluginManager;
import net.minecraft.world.level.storage.loot.LootTable;

public abstract class AbstractClient {
    protected void onLootInfo(SyncLootTableMessage msg) {
        PluginManager.CLIENT_REGISTRY.addLootTable(msg.location(), msg.lootTable(), msg.items());
    }

    protected void onClear(ClearMessage msg) {
        PluginManager.CLIENT_REGISTRY.clearLootData();
    }
}
