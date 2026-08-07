package com.yanny.awi.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.awi.api.IBlockNode;
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
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

public class GenericUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static Map<Identifier, LevelStemNode> decompressWorldgenData(IClientUtils utils, byte[] fullCompressedData, RegistryAccess registryAccess) {
        Map<Identifier, LevelStemNode> worldgenData = new HashMap<>();

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

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(decompressedBuf, registryAccess);

        try {
            utils.getTooltipCache().decode(utils, buf);
            readWorldgenData(utils, buf, worldgenData);

            if (buf.isReadable()) {
                LOGGER.warn("Worldgen payload has {} trailing byte(s) after decoding - the stream is desynchronized!", buf.readableBytes());
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to decode worldgen data! Decoded {} level(s) before failing at reader index {} ({} byte(s) left unread)",
                    worldgenData.size(), buf.readerIndex(), buf.readableBytes(), e);
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
                AbstractClient.INSTANCE.sendWorldgenDataToPlayer(new RequestWorldgenDataMessage());
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
                PluginManager.getInstance().clientRegistry.clearReceivedData();
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

    /**
     * Drops every block the recipe viewer hides from the decoded tree, along with any generation step, placed feature
     * or biome left empty by that. Must run before the tree is handed to a recipe/widget, so that the rendered tree
     * and {@link #collectBlocks} agree on what is visible.
     *
     * @param isVisible viewer-specific visibility test; memoized here because the same block recurs across biomes
     */
    public static void pruneHiddenBlocks(Map<Identifier, LevelStemNode> worldgenData, Predicate<Block> isVisible) {
        Map<Block, Boolean> cache = new IdentityHashMap<>();

        worldgenData.values().removeIf((level) -> level.prune(
                (node) -> !(node instanceof IBlockNode blockNode) || cache.computeIfAbsent(blockNode.getBlock(), isVisible::test)));
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

    public static Component getFormattedCategoryTitle(Identifier location) {
        String translationKey = "dimension." + location.getNamespace() + "." + location.getPath();
        return Component.translatableWithFallback(translationKey, categoryTitle(location));
    }

    private static String categoryTitle(Identifier location) {
        String namespace = location.getNamespace();
        String path = location.getPath();
        String cleanPath = WordUtils.capitalizeFully(path.replace('_', ' '));
        String cleanNamespace = WordUtils.capitalizeFully(namespace.replace('_', ' '));

//        if ("minecraft".equals(namespace)) {
//            return cleanPath;
//        }

        if (cleanNamespace.equalsIgnoreCase(cleanPath) || namespace.replace("_", "").equalsIgnoreCase(path.replace("_", ""))) {
            return cleanPath;
        }

        return cleanNamespace + " › " + cleanPath;
    }

    private static void readWorldgenData(IClientUtils utils, RegistryFriendlyByteBuf readerBuf, Map<Identifier, LevelStemNode> lootData) {
        int levelCount = readerBuf.readInt();

        if (levelCount < 0 || levelCount > 4096) {
            throw new IllegalStateException("Implausible level count " + levelCount + " at reader index "
                    + readerBuf.readerIndex() + " - the stream is desynchronized.");
        }

        for (int i = 0; i < levelCount; i++) {
            int startOfNode = readerBuf.readerIndex();
            Identifier location = readerBuf.readIdentifier();

            try {
                lootData.put(location, (LevelStemNode) utils.getDataNodeFactory(LevelStemNode.ID).apply(utils, readerBuf));
            } catch (Throwable e) {
                LOGGER.error("Failed to decode level {}/{} {} - started at buffer offset {}, failed at {} ({} byte(s) left unread). Aborting, remaining levels will be missing.",
                        i + 1, levelCount, location, startOfNode, readerBuf.readerIndex(), readerBuf.readableBytes(), e);
                throw e;
            }
        }
    }
}
