package com.yanny.ali.network;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.List;

public class SyncLootTableMessage {
    private static final Logger LOGGER = LogUtils.getLogger();

    public final ResourceLocation location;
    public final List<ItemStack> items;

    public final IDataNode node;

    public SyncLootTableMessage(ResourceLocation location, List<ItemStack> items, IDataNode node) {
        this.location = location;
        this.items = items;
        this.node = node;
    }

    public SyncLootTableMessage(FriendlyByteBuf buf) {
        IDataNode dataNode;

        location = buf.readResourceLocation();
        items = buf.readList(FriendlyByteBuf::readItem);

        try {
            IClientUtils utils = PluginManager.CLIENT_REGISTRY;
            dataNode = utils.getDataNodeFactory(LootTableNode.ID).create(utils, buf);
        } catch (Throwable e) {
            LOGGER.error("Failed to decode node for loot table {} with error: {}", location, e.getMessage());
            dataNode = new MissingNode();
        }

        node = dataNode;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        buf.writeCollection(items, FriendlyByteBuf::writeItem);

        IServerUtils utils = PluginManager.SERVER_REGISTRY;

        try {
            node.encode(utils, buf);
        } catch (Throwable e) {
            LOGGER.error("Failed to encode node with error: {}", e.getMessage());
            new MissingNode().encode(utils, buf);
        }
    }
}
