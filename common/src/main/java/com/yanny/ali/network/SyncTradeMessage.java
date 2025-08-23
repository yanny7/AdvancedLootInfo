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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.util.List;

public class SyncTradeMessage implements CustomPacketPayload {
    public static final ResourceLocation ID = Utils.modLoc("trade_sync");
    private static final Logger LOGGER = LogUtils.getLogger();

    public final ResourceLocation location;
    public final IDataNode node;
    public final List<Item> inputs;
    public final List<Item> outputs;

    public SyncTradeMessage(IDataNode node, Pair<List<Item>, List<Item>> items) {
        this.location = new ResourceLocation("empty");
        this.node = node;
        inputs = items.getA();
        outputs = items.getB();
    }

    public SyncTradeMessage(ResourceLocation location, IDataNode node, Pair<List<Item>, List<Item>> items) {
        this.location = location;
        this.node = node;
        inputs = items.getA();
        outputs = items.getB();
    }

    public SyncTradeMessage(FriendlyByteBuf buf) {
        IDataNode dataNode;

        location = buf.readResourceLocation();
        inputs = buf.readList((b) -> BuiltInRegistries.ITEM.get(b.readResourceLocation()));
        outputs = buf.readList((b) -> BuiltInRegistries.ITEM.get(b.readResourceLocation()));

        try {
            IClientUtils utils = PluginManager.CLIENT_REGISTRY;
            dataNode = utils.getNodeFactory(TradeNode.ID).create(utils, buf);
        } catch (Throwable e) {
            LOGGER.error("Failed to decode node for trade {} with error: {}", location, e.getMessage());
            dataNode = new MissingNode();
        }

        node = dataNode;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        buf.writeCollection(inputs, (b, i) -> b.writeResourceLocation(BuiltInRegistries.ITEM.getKey(i)));
        buf.writeCollection(outputs, (b, i) -> b.writeResourceLocation(BuiltInRegistries.ITEM.getKey(i)));

        IServerUtils utils = PluginManager.SERVER_REGISTRY;

        try {
            node.encode(utils, buf);
        } catch (Throwable e) {
            LOGGER.error("Failed to encode trade node with error: {}", e.getMessage());
            new MissingNode().encode(utils, buf);
        }
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }
}
