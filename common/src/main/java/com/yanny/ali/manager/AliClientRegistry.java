package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AliClientRegistry implements IClientRegistry, IClientUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PADDING = 2;

    private final Map<ResourceLocation, IWidgetFactory> widgetMap = new HashMap<>();
    private final Map<ResourceLocation, DataFactory<?>> dataNodeFactoryMap = new HashMap<>();
    private final Map<ResourceLocation, TooltipFactory<?>> tooltipNodeFactoryMap = new HashMap<>();
    private final ICommonUtils utils;

    private final AtomicInteger receivedChunks = new AtomicInteger(0);
    private final AtomicInteger receivedChunksPerSecond = new AtomicInteger(0);
    private ScheduledExecutorService loggerScheduler;

    private volatile DataReceiver currentDataReceiver = null;

    public AliClientRegistry(ICommonUtils utils) {
        this.utils = utils;
    }

    public CompletableFuture<byte[]> getCurrentDataFuture() {
        DataReceiver receiver = currentDataReceiver;

        if (receiver == null) {
            return CompletableFuture.completedFuture(new byte[]{});
        }

        return receiver.getFuture();
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
        if (currentDataReceiver != null && !currentDataReceiver.getFuture().isDone()) {
            LOGGER.warn("Tried to start data reception while another operation is in progress.");
            return;
        }

        currentDataReceiver = new DataReceiver(totalMessages);
        startLogging();

        LOGGER.info("Started receiving loot data");
    }

    public synchronized void clearLootData() {
        if (currentDataReceiver != null) {
            currentDataReceiver.cancelOperation();
            currentDataReceiver = null;
        }

        LOGGER.info("Cleared Loot data");
    }

    public synchronized void doneLootData() {
        DataReceiver receiver = currentDataReceiver;

        if (receiver == null) {
            return;
        }

        receiver.forceDone();
        stopLogging();
        LOGGER.info("Finished receiving loot data");
    }

    @Override
    public void registerWidget(ResourceLocation id, IWidgetFactory factory) {
        widgetMap.put(id, factory);
    }

    @Override
    public <T extends IDataNode> void registerDataNode(ResourceLocation id, DataFactory<T> dataFactory) {
        dataNodeFactoryMap.put(id, dataFactory);
    }

    @Override
    public <T extends ITooltipNode> void registerTooltipNode(ResourceLocation id, TooltipFactory<T> tooltipFactory) {
        tooltipNodeFactoryMap.put(id, tooltipFactory);
    }

    @Override
    public List<IWidget> createWidgets(IWidgetUtils utils, List<IDataNode> entries, RelativeRect parent, int maxWidth) {
        int posX = 0, posY = 0;
        List<IWidget> widgets = new ArrayList<>(entries.size());
        WidgetDirection lastDirection = null;

        for (IDataNode entry : entries) {
            IWidgetFactory widgetFactory = widgetMap.getOrDefault(entry.getId(), widgetMap.get(MissingNode.ID));
            IWidget widget = widgetFactory.create(utils, entry, new RelativeRect(posX, posY, parent.width - posX, 0, parent), maxWidth);
            RelativeRect bounds = widget.getRect();
            WidgetDirection direction = widget.getDirection();

            if (lastDirection == null) {
                if (direction == WidgetDirection.HORIZONTAL) {
                    posX += bounds.width;
                } else {
                    posY += bounds.height + PADDING;
                }
            } else {
                if (lastDirection == WidgetDirection.HORIZONTAL && direction == WidgetDirection.HORIZONTAL) {
                    if (bounds.getRight() <= maxWidth) {
                        posX += bounds.width;
                    } else {
                        posX = bounds.width;
                        posY += widgets.get(widgets.size() - 1).getRect().height;
                        bounds.setOffset(0, posY);
                    }
                } else {
                    posX = 0;

                    if (direction != lastDirection) {
                        if (lastDirection == WidgetDirection.HORIZONTAL) {
                            posY += widgets.get(widgets.size() - 1).getRect().height + PADDING;
                        }

                        bounds.setOffset(posX, posY);
                    }

                    posY += bounds.height + PADDING;
                }
            }

            widgets.add(widget);
            lastDirection = direction;
        }

        int w = 0, h = 0;

        for (IWidget widget : widgets) {
            RelativeRect rect = widget.getRect();

            w = Math.max(w, rect.offsetX + rect.width);
            h = Math.max(h, rect.offsetY + rect.height);
        }

        parent.setDimensions(w, h);
        return widgets;
    }

    @Override
    public <T extends IDataNode> DataFactory<T> getDataNodeFactory(ResourceLocation id) {
        //noinspection unchecked
        DataFactory<T> dataFactory = (DataFactory<T>) dataNodeFactoryMap.get(id);

        return Objects.requireNonNullElseGet(dataFactory, () -> {
            throw new IllegalStateException(String.format("Failed to construct data node - node {%s} was not registered!", id));
        });
    }

    @Override
    public <T extends ITooltipNode> TooltipFactory<T> getTooltipNodeFactory(ResourceLocation id) {
        //noinspection unchecked
        TooltipFactory<T> tooltipFactory = (TooltipFactory<T>) tooltipNodeFactoryMap.get(id);

        return Objects.requireNonNullElseGet(tooltipFactory, () -> {
            throw new IllegalStateException(String.format("Failed to construct tooltip node - node {%s} was not registered!", id));
        });
    }

    @Override
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        return utils.createEntities(type, level);
    }

    public void printRegistrationInfo() {
        LOGGER.info("Registered {} widgets", widgetMap.size());
        LOGGER.info("Registered {} data node factories", dataNodeFactoryMap.size());
        LOGGER.info("Registered {} tooltip node factories", tooltipNodeFactoryMap.size());
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

    private void stopLogging() {
        if (loggerScheduler != null) {
            long count = receivedChunksPerSecond.getAndSet(0);

            LOGGER.info("Received last {} chunk(s). Done receiving data.", count);
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
        private static final long INACTIVITY_TIMEOUT_SECONDS = 30;

        private final CompletableFuture<byte[]> dataFuture = new CompletableFuture<>();
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private final AtomicReference<ScheduledFuture<?>> timeoutHandleRef = new AtomicReference<>();
        private final Map<Integer, byte[]> chunkMap = new HashMap<>();

        private final CountDownLatch completionLatch;

        public DataReceiver(int expectedMessageCount) {
            this.completionLatch = new CountDownLatch(expectedMessageCount);

            resetInactivityTimeout();

            CompletableFuture.runAsync(() -> {
                try {
                    completionLatch.await();

                    if (!dataFuture.isDone()) {
                        completeFuture();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    dataFuture.completeExceptionally(e);
                    shutdownScheduler();
                }
            });
        }

        public void messageReceived(int index, byte[] data) {
            if (dataFuture.isDone()) {
                return;
            }

            chunkMap.put(index, data);
            resetInactivityTimeout();
            completionLatch.countDown();
        }

        public void forceDone() {
            while (completionLatch.getCount() > 0) {
                completionLatch.countDown();
            }

            if (!dataFuture.isDone()) {
                completeFuture();
            }
        }

        private void completeFuture() {
            int totalCompressedSize = chunkMap.values().stream().mapToInt(a -> a.length).sum();
            byte[] fullCompressedData = new byte[totalCompressedSize];
            int offset = 0;

            for (byte[] chunk : chunkMap.values()) {
                System.arraycopy(chunk, 0, fullCompressedData, offset, chunk.length);
                offset += chunk.length;
            }

            dataFuture.complete(fullCompressedData);
            shutdownScheduler();
        }

        private void resetInactivityTimeout() {
            Runnable timeoutTask = () -> {
                if (!dataFuture.isDone()) {
                    String msg = String.format("Data reception failed due to inactivity timeout (%ds).", INACTIVITY_TIMEOUT_SECONDS);

                    dataFuture.completeExceptionally(new TimeoutException(msg));
                    shutdownScheduler();
                }
            };

            ScheduledFuture<?> previousHandle = timeoutHandleRef.getAndSet(null);

            if (previousHandle != null) {
                previousHandle.cancel(false);
            }

            ScheduledFuture<?> newHandle = scheduler.schedule(timeoutTask, INACTIVITY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            timeoutHandleRef.set(newHandle);
        }

        public void cancelOperation() {
            if (!dataFuture.isDone()) {
                dataFuture.cancel(true);
            }

            shutdownScheduler();
        }

        private void shutdownScheduler() {
            ScheduledFuture<?> currentHandle = timeoutHandleRef.getAndSet(null);

            if (currentHandle != null) {
                currentHandle.cancel(false);
            }

            scheduler.shutdownNow();
        }

        public CompletableFuture<byte[]> getFuture() {
            return dataFuture;
        }
    }
}
