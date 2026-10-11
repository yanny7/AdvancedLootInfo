package com.yanny.awi.network;

import com.yanny.aci.CommonLogUtils;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class WorldgenDataReader {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    public static RegistryFriendlyByteBuf decompress(byte[] fullCompressedData, RegistryAccess registryAccess) {
        ByteArrayInputStream bis = new ByteArrayInputStream(fullCompressedData);
        ByteBuf decompressedBuf = Unpooled.buffer();

        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            decompressedBuf.writeBytes(gzip.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new RegistryFriendlyByteBuf(decompressedBuf, registryAccess);
    }

    public static void decode(IClientUtils utils, RegistryFriendlyByteBuf buf, Map<ResourceLocation, LevelStemNode> worldgenData) {
        utils.getTooltipCache().decode(utils, buf);
        readWorldgenData(utils, buf, worldgenData);
    }

    private static void readWorldgenData(IClientUtils utils, RegistryFriendlyByteBuf readerBuf, Map<ResourceLocation, LevelStemNode> worldgenData) {
        int levelCount = readerBuf.readInt();

        if (levelCount < 0 || levelCount > 4096) {
            throw new IllegalStateException("Implausible level count " + levelCount + " at reader index "
                    + readerBuf.readerIndex() + " - the stream is desynchronized.");
        }

        for (int i = 0; i < levelCount; i++) {
            int startOfNode = readerBuf.readerIndex();
            ResourceLocation location = readerBuf.readResourceLocation();

            try {
                worldgenData.put(location, (LevelStemNode) utils.getDataNodeFactory(LevelStemNode.ID).apply(utils, readerBuf));
            } catch (Throwable e) {
                LOGGER.error("Failed to decode level {}/{} {} - started at buffer offset {}, failed at {} ({} byte(s) left unread). Aborting, remaining levels will be missing.",
                        i + 1, levelCount, location, startOfNode, readerBuf.readerIndex(), readerBuf.readableBytes(), e);
                throw e;
            }
        }
    }
}
