package com.yanny.awi.network;

import com.mojang.logging.LogUtils;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.manager.AwiServerRegistry;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public abstract class AbstractServer {
    private static final int MAX_CHUNK_SIZE = 32 * 1024; // 32 KB
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#0.00");
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<WorldgenDataChunkMessage> chunks = new ArrayList<>();

    public void readWorldgenInfo(ServerLevel level) {
        LOGGER.info("Started reading worldgen info");

        long startTime = System.currentTimeMillis();

        AwiServerRegistry serverRegistry = PluginManager.getInstance().serverRegistry;
        RegistryAccess registryAccess = level.registryAccess();
        Registry<LevelStem> levelStemRegistry = registryAccess.registryOrThrow(Registries.LEVEL_STEM);
        Map<ResourceLocation, IDataNode> worldgenNodes = new HashMap<>();

        for (LevelStem levelStem : levelStemRegistry) {
            ResourceLocation location = levelStemRegistry.getKey(levelStem);

            LOGGER.info("Processing dimension: {}", location);
            worldgenNodes.put(location, new LevelStemNode(serverRegistry, levelStem));
        }

        worldgenNodes = removeEmptyNodes(worldgenNodes);

        LOGGER.info("Processing worldgen info took {}ms", System.currentTimeMillis() - startTime);

        ByteBuf rawBuf = Unpooled.buffer();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(rawBuf, registryAccess);

        serverRegistry.getTooltipCache().encode(serverRegistry, buf);
        writeWorldgenData(serverRegistry, buf, worldgenNodes);
        compressAndStoreData(buf);

        serverRegistry.printRuntimeInfo();

        serverRegistry.clearData();
        serverRegistry.getTooltipCache().clear();
    }

    public final void syncLootTables(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            LOGGER.info("Started syncing worldgen info to {}", player.getScoreboardName());
            sendStartMessage(serverPlayer, new StartMessage(chunks.size()));

            for (WorldgenDataChunkMessage message : chunks) {
                try {
                    sendWorldgenDataChunkMessage(serverPlayer, message);
                } catch (Throwable e) {
                    LOGGER.warn("Failed to send message with error: {}", e.getMessage(), e);
                }
            }

            sendDoneMessage(serverPlayer, new DoneMessage());
            LOGGER.info("Finished syncing worldgen info to {}", player.getScoreboardName());
        }
    }

    protected abstract void sendStartMessage(ServerPlayer serverPlayer, StartMessage message);

    protected abstract void sendWorldgenDataChunkMessage(ServerPlayer serverPlayer, WorldgenDataChunkMessage message);

    protected abstract void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message);

    private void writeWorldgenData(IServerUtils utils, RegistryFriendlyByteBuf buf, Map<ResourceLocation, IDataNode> worldgenNodes) {
        int countIndex = buf.writerIndex();
        int successfulNodes = 0;

        buf.writeInt(worldgenNodes.size());

        for (Map.Entry<ResourceLocation, IDataNode> nodeEntry : worldgenNodes.entrySet()) {
            int startOfNode = buf.writerIndex();

            try {
                TooltipContext.set(nodeEntry.getKey());
                buf.writeResourceLocation(nodeEntry.getKey());
                nodeEntry.getValue().encode(utils, buf);
                //TODO write blocks
                successfulNodes++;
            } catch (Throwable e) {
                buf.writerIndex(startOfNode);
                LOGGER.warn("Failed to write worldgen data in {}", nodeEntry.getKey(), e);
            } finally {
                TooltipContext.clear();
            }
        }

        if (successfulNodes != worldgenNodes.size()) {
            int endIndex = buf.writerIndex();

            buf.writerIndex(countIndex);
            buf.writeInt(successfulNodes);
            buf.writerIndex(endIndex);
        }

        worldgenNodes.clear();
    }

    private void compressAndStoreData(ByteBuf rawBuf) {
        int rawSize = rawBuf.readableBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(rawSize);

        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            rawBuf.readBytes(gzip, rawBuf.readableBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] compressedData = bos.toByteArray();
        int totalChunks = (int) Math.ceil((double) compressedData.length / MAX_CHUNK_SIZE);

        for (int i = 0; i < totalChunks; i++) {
            int offset = i * MAX_CHUNK_SIZE;
            int length = Math.min(MAX_CHUNK_SIZE, compressedData.length - offset);
            byte[] chunkData = new byte[length];

            System.arraycopy(compressedData, offset, chunkData, 0, length);
            chunks.add(new WorldgenDataChunkMessage(i, chunkData));
        }

        rawBuf.release();

        LOGGER.info("Compressed worldgen data ({} MB -> {} MB) and stored in {} chunk(s)",
                DOUBLE_FORMAT.format(rawSize / 1024.0 / 1024.0),
                DOUBLE_FORMAT.format(compressedData.length / 1024.0 / 1024.0),
                totalChunks);
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> removeEmptyNodes(Map<ResourceLocation, IDataNode> nodes) {
        Map<ResourceLocation, IDataNode> result = new HashMap<>();
        int emptyNodes = 0;

        for (Map.Entry<ResourceLocation, IDataNode> entry : nodes.entrySet()) {
            IDataNode node = entry.getValue();

            if (node instanceof ListNode listNode) {
                listNode.optimizeList();

                if (!listNode.nodes().isEmpty()) {
                    result.put(entry.getKey(), listNode);
                }
            }
        }

        LOGGER.info("Skipped {} empty or hidden nodes", emptyNodes);
        return result;
    }
}
