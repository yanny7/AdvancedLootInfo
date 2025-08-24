package com.yanny.ali.network;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public record SyncLootTableMessage(ResourceKey<LootTable> location, List<ItemStack> items, IDataNode node) implements CustomPacketPayload {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<SyncLootTableMessage> TYPE = new Type<>(Utils.modLoc("loot_table_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncLootTableMessage> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.LOOT_TABLE), (l) -> l.location,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, (l) -> l.items,
            StreamCodec.of(
                    (b, l) -> {
                        int fallbackIndex = b.writerIndex();
                        try {
                            IServerUtils utils = PluginManager.SERVER_REGISTRY;
                            l.encode(utils, b);
                        } catch (Throwable e) {
                            LOGGER.error("Failed to encode loot table with error: {}", e.getMessage());
                            b.writerIndex(fallbackIndex);
                            ByteBufCodecs.fromCodecWithRegistries(LootDataType.TABLE.codec()).encode(b, LootTable.EMPTY);
                        }
                    },
                    (b) -> {
                        try {
                            IClientUtils utils = PluginManager.CLIENT_REGISTRY;
                            return new LootTableNode(utils, b);
                        } catch (Throwable e) {
                            LOGGER.error("Failed to decode loot table with error: {}", e.getMessage());
                            return new MissingNode();
                        }
                    }
            ), (l) -> l.node,
            SyncLootTableMessage::new
    );

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
