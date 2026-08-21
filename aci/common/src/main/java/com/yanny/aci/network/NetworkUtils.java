package com.yanny.aci.network;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.ICoreDataNode;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.tooltip.TooltipContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.zip.GZIPOutputStream;

public class NetworkUtils {
    private static final int MAX_CHUNK_SIZE = 32 * 1024; // 32 KB
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#0.00");

    public static void compressAndStoreData(String modId, ByteBuf rawBuf, BiConsumer<Integer, byte[]> chunkConsumer) {
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
            chunkConsumer.accept(i, chunkData);
        }

        rawBuf.release();

        CommonLogUtils.getLogger(modId).info("Compressed data ({} MB -> {} MB) and stored in {} chunk(s)",
                DOUBLE_FORMAT.format(rawSize / 1024.0 / 1024.0),
                DOUBLE_FORMAT.format(compressedData.length / 1024.0 / 1024.0),
                totalChunks);
    }

    public static <
            TServerUtils extends ICoreServerUtils<?>,
            TNode extends ICoreDataNode<TServerUtils>
            > void writeMapData(String modId, TServerUtils utils, RegistryFriendlyByteBuf buf, Map<ResourceLocation, TNode> nodes) {
        int countIndex = buf.writerIndex();
        int successfulNodes = 0;

        buf.writeInt(nodes.size());

        for (Map.Entry<ResourceLocation, TNode> nodeEntry : nodes.entrySet()) {
            if (writeEntryData(modId, utils, buf, nodeEntry)) {
                successfulNodes++;
            }
        }

        if (successfulNodes != nodes.size()) {
            CommonLogUtils.getLogger(modId).warn("Only {} of {} node(s) were encoded successfully", successfulNodes, nodes.size());

            int endIndex = buf.writerIndex();

            buf.writerIndex(countIndex);
            buf.writeInt(successfulNodes);
            buf.writerIndex(endIndex);
        }

        nodes.clear();
    }

    public static <
            TServerUtils extends ICoreServerUtils<?>,
            TNode extends ICoreDataNode<TServerUtils>
            > boolean writeEntryData(String modId, TServerUtils utils, RegistryFriendlyByteBuf buf, Map.Entry<ResourceLocation, TNode> nodeEntry) {
        int startOfEntry = buf.writerIndex();

        buf.writeResourceLocation(nodeEntry.getKey());

        if (writeNodeData(modId, utils, buf, nodeEntry.getKey(), nodeEntry.getValue())) {
            return true;
        }

        buf.writerIndex(startOfEntry);
        return false;
    }

    public static <
            TServerUtils extends ICoreServerUtils<?>,
            TNode extends ICoreDataNode<TServerUtils>
            > boolean writeNodeData(String modId, TServerUtils utils, RegistryFriendlyByteBuf buf, ResourceLocation id, TNode node) {
        int startOfNode = buf.writerIndex();

        try {
            TooltipContext.set(id);
            node.encode(utils, buf);
            return true;
        } catch (Throwable e) {
            buf.writerIndex(startOfNode);
            CommonLogUtils.getLogger(modId).warn("Failed to write data in {}", id, e);
            return false;
        } finally {
            TooltipContext.clear(); // executed right before return
        }
    }
}
