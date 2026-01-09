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

    private final Map<ResourceLocation, IWidgetFactory> widgetMap = new HashMap<>();
    private final Map<ResourceLocation, DataFactory<?>> dataNodeFactoryMap = new HashMap<>();
    private final Map<ResourceLocation, TooltipFactory<?>> tooltipNodeFactoryMap = new HashMap<>();
    private final ICommonUtils utils;

    private final AtomicInteger receivedChunks = new AtomicInteger(0);
    private final AtomicInteger receivedChunksPerSecond = new AtomicInteger(0);
    private ScheduledExecutorService loggerScheduler;

    private volatile DataReceiver currentDataReceiver = null;

    private final AtomicReference<CompletableFuture<byte[]>> activeDataPromise = new AtomicReference<>(new CompletableFuture<>());

    public AliClientRegistry(ICommonUtils utils) {
        this.utils = utils;
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
        clearLootData();

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

    public synchronized void clearLootData() {
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
        LOGGER.info("Expecting reload loot data");
        clearLootData();
    }

    public synchronized void logOut() {
        LOGGER.info("Player log out received");
        clearLootData();
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
            IWidget widget = widgetFactory.create(utils, entry, new RelativeRect(posX, posY, parent.getWidth() - posX, 0, parent), maxWidth);
            RelativeRect bounds = widget.getRect();
            WidgetDirection direction = widget.getDirection();

            if (lastDirection == null) {
                if (direction == WidgetDirection.HORIZONTAL) {
                    posX += bounds.getWidth();
                } else {
                    posY += bounds.getHeight() + IWidget.PADDING;
                }
            } else {
                if (lastDirection == WidgetDirection.HORIZONTAL && direction == WidgetDirection.HORIZONTAL) {
                    if (bounds.getRight() <= maxWidth) {
                        posX += bounds.getWidth();
                    } else {
                        posX = bounds.getWidth();
                        posY += widgets.get(widgets.size() - 1).getRect().getHeight();
                        bounds.setOffset(0, posY);
                    }
                } else {
                    posX = 0;

                    if (direction != lastDirection) {
                        if (lastDirection == WidgetDirection.HORIZONTAL) {
                            posY += widgets.get(widgets.size() - 1).getRect().getHeight() + IWidget.PADDING;
                        }

                        bounds.setOffset(posX, posY);
                        widget.onResize(bounds, maxWidth);
                    }

                    if (direction != WidgetDirection.HORIZONTAL) {
                        posY += bounds.getHeight() + IWidget.PADDING;
                    } else {
                        posX += bounds.getWidth();
                    }
                }
            }

            widgets.add(widget);
            lastDirection = direction;
        }

        int w = 0, h = 0;

        for (IWidget widget : widgets) {
            RelativeRect rect = widget.getRect();

            w = Math.max(w, rect.getOffsetX() + rect.getWidth());
            h = Math.max(h, rect.getOffsetY() + rect.getHeight());
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
        private final Map<Integer, byte[]> chunkMap = new HashMap<>();
        private final CountDownLatch completionLatch;

        public DataReceiver(int expectedMessageCount) {
            this.completionLatch = new CountDownLatch(expectedMessageCount);

            CompletableFuture.runAsync(() -> {
                try {
                    completionLatch.await();

                    if (!dataFuture.isDone()) {
                        completeFuture();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    dataFuture.completeExceptionally(e);
                }
            });
        }

        public void messageReceived(int index, byte[] data) {
            if (dataFuture.isDone()) {
                return;
            }

            chunkMap.put(index, data);
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
        }

        public void cancelOperation() {
            if (!dataFuture.isDone()) {
                dataFuture.cancel(true);
            }
        }

        public CompletableFuture<byte[]> getFuture() {
            return dataFuture;
        }
    }
}
