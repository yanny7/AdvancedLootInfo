package com.yanny.ali.network;

import com.mojang.serialization.JsonOps;
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
        lootTable = LootDataType.TABLE.codec.parse(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).get().orThrow();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, LootDataType.TABLE.codec.encodeStart(JsonOps.INSTANCE, lootTable).get().orThrow());
    }
}
