package com.yanny.ali.network;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public class InfoSyncLootTableMessage {
    public final ResourceLocation location;
    public final LootTable lootTable;
    public final List<Item> items;

    public InfoSyncLootTableMessage(ResourceLocation location, LootTable lootTable, List<Item> items) {
        this.location = location;
        this.lootTable = lootTable;
        this.items = items;
    }

    public InfoSyncLootTableMessage(FriendlyByteBuf buf) {
        location = buf.readResourceLocation();
        lootTable = LootDataType.TABLE.codec.parse(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).get().orThrow();
        items = buf.readList((b) -> BuiltInRegistries.ITEM.get(b.readResourceLocation()));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, LootDataType.TABLE.codec.encodeStart(JsonOps.INSTANCE, lootTable).get().orThrow());
        buf.writeCollection(items, (b, item) -> buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item)));
    }
}
