package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.aci.manager.CoreClientRegistry;
import com.yanny.ali.api.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.plugin.client.widget.MissingWidget;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AliClientRegistry extends CoreClientRegistry<AliConfig, ICommonUtils, ITooltipNode, IDataNode, IWidgetUtils, IClientUtils> implements IClientRegistry, IClientUtils, ICommonUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long RELOAD_COOLDOWN_MS = 2000L;

    private final AtomicInteger receivedChunks = new AtomicInteger(0);
    private final AtomicInteger receivedChunksPerSecond = new AtomicInteger(0);
    private ScheduledExecutorService loggerScheduler;
    private final AtomicInteger syncedTagCount = new AtomicInteger(0);
    private final AtomicBoolean loggedIn = new AtomicBoolean(false);
    private long lastSyncStartTime = 0;

    private volatile DataReceiver currentDataReceiver = null;

    private final AtomicReference<CompletableFuture<byte[]>> activeDataPromise = new AtomicReference<>(new CompletableFuture<>());

    public AliClientRegistry(ICommonUtils utils) {
        super(utils);
    }

    public CompletableFuture<byte[]> getCurrentDataFuture() {
        return activeDataPromise.get();
    }

    public void addChunkData(int index, byte[] data) {
        DataReceiver receiver = currentDataReceiver;

        if (receiver == null) {
            return;
        }

        receivedChunks.incrementAndGet();
        receivedChunksPerSecond.incrementAndGet();
        receiver.messageReceived(index, data);
    }

    public synchronized void startLootData(int totalMessages) {
        clearLootData(true);

        lastSyncStartTime = System.currentTimeMillis();
        currentDataReceiver = new DataReceiver(totalMessages);

        CompletableFuture<byte[]> currentPromise = activeDataPromise.get();

        currentDataReceiver.getFuture().whenComplete((data, throwable) -> {
            if (throwable != null) {
                currentPromise.completeExceptionally(throwable);
            } else {
                currentPromise.complete(data);
            }
        });

        startLogging();
        LOGGER.info("Started receiving loot data");
    }

    public synchronized void clearLootData(boolean force) {
        long timeSinceStart = System.currentTimeMillis() - lastSyncStartTime;

        if (timeSinceStart < RELOAD_COOLDOWN_MS && !force) {
            LOGGER.info("Ignoring redundant reload request (triggered only {}ms after sync start)", timeSinceStart);
            return;
        }

        if (currentDataReceiver != null) {
            currentDataReceiver.cancelOperation();
            currentDataReceiver = null;
            stopLogging(true);
        }

        CompletableFuture<byte[]> oldPromise = activeDataPromise.getAndSet(new CompletableFuture<>());

        if (!oldPromise.isDone()) {
            oldPromise.cancel(true);
        }

        LOGGER.info("Cleared Loot data");
    }

    public synchronized void reloadLootData() {
        // reload is called on login, causing clearing already received data
        if (loggedIn.get() && syncedTagCount.getAndIncrement() > 0) {
            LOGGER.info("Reloading loot data");
            clearLootData(false);
        }
    }

    public synchronized void loggingIn(boolean modAvailableOnServer) {
        LOGGER.info("Player login received");
        loggedIn.set(true);

        if (!modAvailableOnServer) {
            LOGGER.info("ALI is not present on the server. Completing sync with empty data.");
            CompletableFuture<byte[]> currentPromise = activeDataPromise.get();

            if (!currentPromise.isDone()) {
                currentPromise.complete(new byte[0]);
            }

            if (currentDataReceiver != null) {
                currentDataReceiver.cancelOperation();
                currentDataReceiver = null;
                stopLogging(true);
            }
        }
    }

    public synchronized void loggingOut() {
        LOGGER.info("Player logout received");
        clearLootData(true);
        loggedIn.set(false);
        syncedTagCount.set(0);
        lastSyncStartTime = 0;
    }

    public synchronized void doneLootData() {
        DataReceiver receiver = currentDataReceiver;

        if (receiver == null) {
            return;
        }

        receiver.forceDone();
        stopLogging(false);
        LOGGER.info("Finished receiving loot data");
    }

    @NotNull
    @Override
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        return commonUtils.createEntities(type, level);
    }

    @NotNull
    @Override
    public IWidgetFactory<IDataNode, IWidgetUtils> getMissingWidgetFactory() {
        return MissingWidget::new;
    }

    private void startLogging() {
        Runnable logTask = () -> {
            long count = receivedChunksPerSecond.getAndSet(0);

            LOGGER.info("Received {} chunk(s) per second", count);
        };

        receivedChunks.set(0);
        receivedChunksPerSecond.set(0);
        loggerScheduler = Executors.newSingleThreadScheduledExecutor();
        loggerScheduler.scheduleAtFixedRate(logTask, 1, 1, TimeUnit.SECONDS);
    }

    private void stopLogging(boolean forcedStop) {
        if (loggerScheduler != null) {
            long count = receivedChunksPerSecond.getAndSet(0);

            if (!forcedStop) {
                LOGGER.info("Received last {} chunk(s). Done receiving data.", count);
            }

            loggerScheduler.shutdownNow();

            try {
                if (!loggerScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.warn("Logging scheduler didn't stop in time!");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class DataReceiver {
        private final CompletableFuture<byte[]> dataFuture = new CompletableFuture<>();
        private final Map<Integer, byte[]> chunkMap = new ConcurrentHashMap<>();
        private final int totalChunks;
        private final AtomicInteger receivedChunksCount = new AtomicInteger(0);

        public DataReceiver(int expectedMessageCount) {
            totalChunks = expectedMessageCount;
        }

        public void messageReceived(int index, byte[] data) {
            if (dataFuture.isDone()) {
                return;
            }

            chunkMap.put(index, data);

            if (receivedChunksCount.incrementAndGet() == totalChunks) {
                completeFuture();
            }
        }

        public void forceDone() {
            if (!dataFuture.isDone()) {
                String errorMsg = String.format("Incomplete loot data! Expected %d chunks, but received only %d. Data is unusable.", totalChunks, receivedChunksCount.get());

                LOGGER.error(errorMsg);
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
}
