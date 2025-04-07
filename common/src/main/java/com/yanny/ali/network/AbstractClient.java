package com.yanny.ali.network;

import com.yanny.ali.manager.PluginManager;
import net.minecraft.world.level.storage.loot.LootTable;

public abstract class AbstractClient {
    protected void onLootInfo(InfoSyncLootTableMessage msg) {
        LootTable lootTable = msg.lootTable();
        PluginManager.CLIENT_REGISTRY.addLootTable(msg.location(), lootTable);
    }

    protected void onClear(ClearMessage msg) {
        PluginManager.CLIENT_REGISTRY.clearLootTables();
    }
}
