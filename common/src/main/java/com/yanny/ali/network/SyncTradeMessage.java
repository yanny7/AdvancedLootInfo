package com.yanny.ali.network;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.util.List;

public record SyncTradeMessage(ResourceLocation location, IDataNode node, List<Item> inputs, List<Item> outputs) implements CustomPacketPayload {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static final Type<SyncTradeMessage> TYPE = new Type<>(Utils.modLoc("trade_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTradeMessage> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, (l) -> l.location,
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
                            return new TradeNode(utils, b);
                        } catch (Throwable e) {
                            LOGGER.error("Failed to decode loot table with error: {}", e.getMessage());
                            return new MissingNode();
                        }
                    }
            ), (l) -> l.node,
            StreamCodec.of(
                    (buf, l) -> buf.writeCollection(l, (b, i) -> b.writeResourceLocation(BuiltInRegistries.ITEM.getKey(i))),
                    (buf) -> buf.readList((b) -> BuiltInRegistries.ITEM.getValue(b.readResourceLocation()))
            ), (l) -> l.inputs,
            StreamCodec.of(
                    (buf, l) -> buf.writeCollection(l, (b, i) -> b.writeResourceLocation(BuiltInRegistries.ITEM.getKey(i))),
                    (buf) -> buf.readList((b) -> BuiltInRegistries.ITEM.getValue(b.readResourceLocation()))
            ), (l) -> l.outputs,
            SyncTradeMessage::new
    );

    public SyncTradeMessage(IDataNode node, Pair<List<Item>, List<Item>> items) {
        this(ResourceLocation.withDefaultNamespace("empty"), node, items.getA(), items.getB());
    }

    public SyncTradeMessage(ResourceLocation location, IDataNode node, Pair<List<Item>, List<Item>> items) {
        this(location, node, items.getA(), items.getB());
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
