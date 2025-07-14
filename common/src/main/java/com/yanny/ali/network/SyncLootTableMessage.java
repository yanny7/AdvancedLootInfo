package com.yanny.ali.network;

import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

import java.util.List;

public record SyncLootTableMessage(ResourceKey<LootTable> location, IDataNode node, List<Item> items) implements CustomPacketPayload {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<InfoSyncLootTableMessage> TYPE = new Type<>(Utils.modLoc("loot_table_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InfoSyncLootTableMessage> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.LOOT_TABLE), (l) -> l.location,
            StreamCodec.of(
                    (b, l) -> {
                        int fallbackIndex = b.writerIndex();
                        try {
                            ByteBufCodecs.fromCodecWithRegistries(LootDataType.TABLE.codec()).encode(b, l);
                        } catch (Throwable e) {
                            LOGGER.error("Failed to encode loot table with error: {}", e.getMessage());
                            b.writerIndex(fallbackIndex);
                            ByteBufCodecs.fromCodecWithRegistries(LootDataType.TABLE.codec()).encode(b, LootTable.EMPTY);
                        }
                    },
                    (b) -> {
                        try {
                            return ByteBufCodecs.fromCodecWithRegistries(LootDataType.TABLE.codec()).decode(b);
                        } catch (Throwable e) {
                            LOGGER.error("Failed to decode loot table with error: {}", e.getMessage());
                            return LootTable.EMPTY;
                        }
                    }),
            (l) -> l.lootTable,
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

/*
    public SyncLootTableMessage(FriendlyByteBuf buf) {
        IDataNode dataNode;

        location = buf.readResourceLocation();
        items = buf.readList((b) -> BuiltInRegistries.ITEM.get(b.readResourceLocation()));

        try {
            IClientUtils utils = PluginManager.CLIENT_REGISTRY;
            dataNode = utils.getNodeFactory(LootTableNode.ID).create(utils, buf);
        } catch (Throwable e) {
            LOGGER.error("Failed to decode node for loot table {} with error: {}", location, e.getMessage());
            dataNode = new MissingNode();
        }

        node = dataNode;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        buf.writeCollection(items, (b, item) -> b.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item)));

        IServerUtils utils = PluginManager.SERVER_REGISTRY;

        try {
            node.encode(utils, buf);
        } catch (Throwable e) {
            LOGGER.error("Failed to encode node with error: {}", e.getMessage());
            new MissingNode().encode(utils, buf);
        }
    }
 */