package com.yanny.ali.network;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;

public abstract class AbstractClient {
    protected void onLootInfo(InfoSyncLootTableMessage msg, HolderLookup.Provider provider) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, provider);
        LootTable lootTable = LootDataType.TABLE.codec().parse(ops, msg.lootTable).getOrThrow();
        PluginManager.CLIENT_REGISTRY.addLootTable(msg.location, lootTable);
    }

    protected void onClear(ClearMessage msg) {
        PluginManager.CLIENT_REGISTRY.clearLootTables();
    }
}
