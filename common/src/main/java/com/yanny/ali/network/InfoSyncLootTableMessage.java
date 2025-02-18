package com.yanny.ali.network;

import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.entry.LootTableEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class InfoSyncLootTableMessage {
    public final ResourceLocation location;
    public final LootTableEntry value;

    public InfoSyncLootTableMessage(ResourceLocation location, LootTableEntry value) {
        this.location = location;
        this.value = value;
    }

    public InfoSyncLootTableMessage(FriendlyByteBuf buf) {
        location = buf.readResourceLocation();
        value = new LootTableEntry(new AliContext(null, PluginManager.COMMON_REGISTRY, null), buf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        value.encode(new AliContext(null, PluginManager.COMMON_REGISTRY, null), buf);
    }
}
