package com.yanny.ali.network;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SyncLootTableMessage implements CustomPacketPayload {
    public static final ResourceLocation ID = Utils.modLoc("loot_table_sync");
    private static final Logger LOGGER = LogUtils.getLogger();

    public final ResourceLocation location;
    public final List<Item> items;

    public final IDataNode node;

    public SyncLootTableMessage(ResourceLocation location, List<Item> items, IDataNode node) {
        this.location = location;
        this.items = items;
        this.node = node;
    }

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

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static String compressString(String input) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(input.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("Failed to compress loot table with error: {}", e.getMessage());
            throw new RuntimeException();
        }

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static String decompressString(String compressedBase64) {
        byte[] compressedBytes = Base64.getDecoder().decode(compressedBase64);
        ByteArrayInputStream bais = new ByteArrayInputStream(compressedBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
            byte[] buffer = new byte[1024];
            int len;

            while ((len = gzipIn.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to decompress loot table with error: {}", e.getMessage());
            throw new RuntimeException();
        }

        return baos.toString(StandardCharsets.UTF_8);
    }
}
