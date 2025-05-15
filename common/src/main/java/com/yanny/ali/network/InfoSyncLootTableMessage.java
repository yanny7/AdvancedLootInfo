package com.yanny.ali.network;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class InfoSyncLootTableMessage {
    private static final Logger LOGGER = LogUtils.getLogger();

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
        lootTable = LootDataType.TABLE.parser().fromJson(decompressString(buf.readUtf()), LootTable.class);
        items = buf.readList((b) -> BuiltInRegistries.ITEM.get(b.readResourceLocation()));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(location);
        buf.writeUtf(compressString(LootDataType.TABLE.parser().toJsonTree(lootTable).toString()));
        buf.writeCollection(items, (b, item) -> buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item)));
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
