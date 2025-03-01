package com.yanny.ali.network;

import com.yanny.ali.Utils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.entry.LootTableEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class InfoSyncLootTableMessage implements CustomPacketPayload {
    public static final ResourceLocation ID = Utils.modLoc("loot_table_sync");

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

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        value.encode(new AliContext(null, PluginManager.COMMON_REGISTRY, null), buf);
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }
}
