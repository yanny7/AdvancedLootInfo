package com.yanny.aci.compatibility;

import com.yanny.aci.CommonLogUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DataReceiver {
    private final Logger logger;
    private final CompletableFuture<byte[]> dataFuture = new CompletableFuture<>();
    private final Map<Integer, byte[]> chunkMap = new ConcurrentHashMap<>();
    private final int totalChunks;
    private final AtomicInteger receivedChunksCount = new AtomicInteger(0);

    public DataReceiver(String modId, int expectedMessageCount) {
        logger = CommonLogUtils.getLogger(modId);
        totalChunks = expectedMessageCount;
    }

    public void messageReceived(int index, byte[] data) {
        if (dataFuture.isDone()) {
            return;
        }

        if (chunkMap.put(index, data) != null) {
            logger.warn("Received duplicate chunk with index {}, ignoring", index);
            return;
        }

        if (receivedChunksCount.incrementAndGet() == totalChunks) {
            completeFuture();
        }
    }

    public void forceDone() {
        if (!dataFuture.isDone()) {
            String errorMsg = String.format("Incomplete data! Expected %d chunks, but received only %d. Data is unusable.", totalChunks, receivedChunksCount.get());

            logger.error(errorMsg);
            dataFuture.completeExceptionally(new IllegalStateException(errorMsg));
        }
    }

    private void completeFuture() {
        if (dataFuture.isDone()) {
            return;
        }

        int totalCompressedSize = chunkMap.values().stream().mapToInt(a -> a.length).sum();
        byte[] fullCompressedData = new byte[totalCompressedSize];
        int offset = 0;

        for (int i = 0; i < totalChunks; i++) {
            byte[] chunk = chunkMap.get(i);

            if (chunk == null) {
                String errorMsg = String.format("Missing chunk with index %d out of %d. Data is unusable.", i, totalChunks);

                logger.error(errorMsg);
                chunkMap.clear();
                dataFuture.completeExceptionally(new IllegalStateException(errorMsg));
                return;
            }

            System.arraycopy(chunk, 0, fullCompressedData, offset, chunk.length);
            offset += chunk.length;
        }

        chunkMap.clear();
        dataFuture.complete(fullCompressedData);
    }

    public void cancelOperation() {
        if (!dataFuture.isDone()) {
            dataFuture.cancel(true);
        }

        chunkMap.clear();
    }

    public CompletableFuture<byte[]> getFuture() {
        return dataFuture;
    }
}
