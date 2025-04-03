package com.yanny.ali.network;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

public class InfoSyncLootTableMessage implements CustomPacketPayload {
    public static final ResourceLocation ID = Utils.modLoc("loot_table_sync");

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

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, LootDataType.TABLE.codec.encodeStart(JsonOps.INSTANCE, lootTable).get().orThrow());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }
}
