package com.yanny.aci.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.function.BiConsumer;
import java.util.zip.GZIPOutputStream;

public class NetworkUtils {
    private static final int MAX_CHUNK_SIZE = 32 * 1024; // 32 KB
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#0.00");
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void compressAndStoreData(ByteBuf rawBuf, String dataType, BiConsumer<Integer, byte[]> chunkConsumer) {
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

        LOGGER.info("Compressed {} data ({} MB -> {} MB) and stored in {} chunk(s)",
                dataType,
                DOUBLE_FORMAT.format(rawSize / 1024.0 / 1024.0),
                DOUBLE_FORMAT.format(compressedData.length / 1024.0 / 1024.0),
                totalChunks);
    }
}
