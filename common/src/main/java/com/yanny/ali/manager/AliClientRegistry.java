package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AliClientRegistry implements IClientRegistry, IClientUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<Identifier, IWidgetFactory> widgetMap = new HashMap<>();
    private final Map<Identifier, DataFactory<?>> dataNodeFactoryMap = new HashMap<>();
    private final Map<Identifier, TooltipFactory<?>> tooltipNodeFactoryMap = new HashMap<>();
    private final ICommonUtils utils;

    private final AtomicInteger receivedChunks = new AtomicInteger(0);
    private final AtomicInteger receivedChunksPerSecond = new AtomicInteger(0);
    private ScheduledExecutorService loggerScheduler;
    private final AtomicInteger syncedTagCount = new AtomicInteger(0);
    private final AtomicBoolean loggedIn = new AtomicBoolean(false);

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
        // reload is called on login, causing clearing already received data
        if (loggedIn.get() && syncedTagCount.getAndIncrement() > 0) {
            LOGGER.info("Reloading loot data");
            clearLootData();
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
        clearLootData();
        loggedIn.set(false);
        syncedTagCount.set(0);
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
    public void registerWidget(Identifier id, IWidgetFactory factory) {
        widgetMap.put(id, factory);
    }

    @Override
    public <T extends IDataNode> void registerDataNode(Identifier id, DataFactory<T> dataFactory) {
        dataNodeFactoryMap.put(id, dataFactory);
    }

    @Override
    public <T extends ITooltipNode> void registerTooltipNode(Identifier id, TooltipFactory<T> tooltipFactory) {
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
    public <T extends IDataNode> DataFactory<T> getDataNodeFactory(Identifier id) {
        //noinspection unchecked
        DataFactory<T> dataFactory = (DataFactory<T>) dataNodeFactoryMap.get(id);

        return Objects.requireNonNullElseGet(dataFactory, () -> {
            throw new IllegalStateException(String.format("Failed to construct data node - node {%s} was not registered!", id));
        });
    }

    @Override
    public <T extends ITooltipNode> TooltipFactory<T> getTooltipNodeFactory(Identifier id) {
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

    @Override
    public AliConfig getConfiguration() {
        return utils.getConfiguration();
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
