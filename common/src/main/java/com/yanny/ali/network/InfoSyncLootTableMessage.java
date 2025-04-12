package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record InfoSyncLootTableMessage(ResourceKey<LootTable> location, LootTable lootTable, List<Item> items) implements CustomPacketPayload {
    public static final Type<InfoSyncLootTableMessage> TYPE = new Type<>(Utils.modLoc("loot_table_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InfoSyncLootTableMessage> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.LOOT_TABLE), (l) -> l.location,
            ByteBufCodecs.fromCodecWithRegistries(LootDataType.TABLE.codec()), (l) -> l.lootTable,
            StreamCodec.of(
                    (b, l) -> b.writeCollection(l, (a, i) -> a.writeResourceLocation(BuiltInRegistries.ITEM.getKey(i))),
                    (b) -> b.readList((a) -> BuiltInRegistries.ITEM.get(a.readResourceLocation()))
            ), (l) -> l.items,
            InfoSyncLootTableMessage::new
    );

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
