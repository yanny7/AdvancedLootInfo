package com.yanny.awi.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.network.AbstractClient;
import com.yanny.awi.network.RequestWorldgenDataMessage;
import com.yanny.awi.plugin.common.nodes.BlockNode;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.zip.GZIPInputStream;

public class GenericUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static Map<ResourceLocation, LevelStemNode> decompressWorldgenData(IClientUtils utils, byte[] fullCompressedData) {
        Map<ResourceLocation, LevelStemNode> worldgenData = new HashMap<>();

        if (fullCompressedData.length == 0) {
            return worldgenData;
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(fullCompressedData);
        ByteBuf decompressedBuf = Unpooled.buffer();

        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            decompressedBuf.writeBytes(gzip.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FriendlyByteBuf buf = new FriendlyByteBuf(decompressedBuf);

        try {
            utils.getTooltipCache().decode(utils, buf);
            readWorldgenData(utils, buf, worldgenData);
        } catch (Throwable e) {
            LOGGER.warn("Failed to decode worldgen data!", e);
        } finally {
            buf.release();
        }

        return worldgenData;
    }

    public static <T> void register(T emiRegistry, BiConsumer<T, byte[]> registerData) {
        LOGGER.info("Starting data registration...");
        int maxRetries = 3;
        int currentTry = 0;

        while (currentTry < maxRetries) {
            currentTry++;
            CompletableFuture<byte[]> futureData = PluginManager.getInstance().clientRegistry.getCurrentDataFuture();

            if (!futureData.isDone()) {
                LOGGER.info("Data not ready. Requesting data from server (Attempt {}/{})", currentTry, maxRetries);
                AbstractClient.INSTANCE.sendLootDataToPlayer(new RequestWorldgenDataMessage());
            } else {
                LOGGER.info("Data already received, processing instantly.");
            }

            try {
                byte[] fullCompressedData = futureData.get(30, TimeUnit.SECONDS);

                registerData.accept(emiRegistry, fullCompressedData);
                LOGGER.info("Data registration finished successfully.");
                return;
            } catch (TimeoutException e) {
                LOGGER.warn("Timeout while waiting for server data! The server didn't respond in time or packets were lost.", e);
                PluginManager.getInstance().clientRegistry.clearLootData();
            } catch (CancellationException e) {
                LOGGER.warn("Data reception was cancelled. Retrying with new data stream...", e);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Registration thread interrupted!", e);
                return;
            } catch (ExecutionException e) {
                LOGGER.error("Failed to finish registering data with error", e);
                return;
            } catch (Throwable e) {
                LOGGER.error("Failed to finish registering data with unexpected error", e);
                return;
            }
        }

        LOGGER.error("CRITICAL: Could not fetch loot data from server after {} attempts. Recipe viewer integration will be empty or incomplete.", maxRetries);
    }

    @NotNull
    public static List<Block> collectBlocks(IDataNode node) {
        List<Block> blocks = new ArrayList<>();

        if (node instanceof ListNode listNode) {
            for (IDataNode iDataNode : listNode.nodes()) {
                blocks.addAll(collectBlocks(iDataNode));
            }
        } else if (node instanceof BlockNode blockNode) {
            blocks.add(blockNode.getBlock());
        }

        return blocks;
    }

    private static void readWorldgenData(IClientUtils utils, FriendlyByteBuf readerBuf, Map<ResourceLocation, LevelStemNode> lootData) {
        int lootDataCount = readerBuf.readInt();

        for (int i = 0; i < lootDataCount; i++) {
            ResourceLocation location = readerBuf.readResourceLocation();
            LevelStemNode dataNode = (LevelStemNode) utils.getDataNodeFactory(LevelStemNode.ID).apply(utils, readerBuf);

            lootData.put(location, dataNode);
        }
    }
}
