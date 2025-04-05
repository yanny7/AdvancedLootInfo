package com.yanny.ali.network;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.Utils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

public class InfoSyncLootTableMessage implements CustomPacketPayload {
    public static final Type<InfoSyncLootTableMessage> TYPE = new Type<>(Utils.modLoc("loot_table_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InfoSyncLootTableMessage> CODEC = new StreamCodec<>() {
        @NotNull
        @Override
        public InfoSyncLootTableMessage decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new InfoSyncLootTableMessage(buf);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull InfoSyncLootTableMessage message) {
            message.write(buf);
        }
    };

    public final ResourceKey<LootTable> location;
    public final JsonElement lootTable;

    public InfoSyncLootTableMessage(ResourceKey<LootTable> location, LootTable lootTable, HolderLookup.Provider provider) {
        this.location = location;
        this.lootTable = LootDataType.TABLE.codec().encodeStart(RegistryOps.create(JsonOps.INSTANCE, provider), lootTable).getOrThrow();
    }

    public InfoSyncLootTableMessage(FriendlyByteBuf buf) {
        location = buf.readResourceKey(Registries.LOOT_TABLE);
        lootTable = buf.readJsonWithCodec(ExtraCodecs.JSON);
//        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, ClientRegistryLayer.createRegistryAccess().compositeAccess());
//        lootTable = LootDataType.TABLE.codec().parse(ops, element).getOrThrow();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceKey(location);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, lootTable);
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
