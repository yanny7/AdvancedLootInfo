package com.yanny.ali.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;

public class InfoSyncLootTableMessage {
    public final ResourceLocation location;
    public final LootTable lootTable;

    public InfoSyncLootTableMessage(ResourceLocation location, LootTable lootTable) {
        this.location = location;
        this.lootTable = lootTable;
    }

    public InfoSyncLootTableMessage(FriendlyByteBuf buf) {
        location = buf.readResourceLocation();
        lootTable = LootDataType.TABLE.parser().fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON), LootTable.class);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, LootDataType.TABLE.parser().toJsonTree(lootTable));
    }
}
