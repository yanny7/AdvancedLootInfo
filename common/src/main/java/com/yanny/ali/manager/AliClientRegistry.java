package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AliClientRegistry implements IClientRegistry, IClientUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PADDING = 2;

    private final Map<ResourceLocation, IWidgetFactory> widgetMap = new HashMap<>();
    private final Map<ResourceLocation, NodeFactory<?>> nodeFactoryMap = new HashMap<>();
    private final Map<ResourceLocation, List<ItemStack>> lootItemMap = new HashMap<>();
    private final Map<ResourceLocation, IDataNode> lootNodeMap = new HashMap<>();
    private final Map<ResourceLocation, IDataNode> lootTradeMap = new HashMap<>();
    private final Map<ResourceLocation, List<Item>> tradeInputItemMap = new HashMap<>();
    private final Map<ResourceLocation, List<Item>> tradeOutputItemMap = new HashMap<>();
    private final ICommonUtils utils;

    private final AtomicInteger receivedMessages = new AtomicInteger(0);
    private final AtomicInteger receivedMessagesPerSecond = new AtomicInteger(0);
    private ScheduledExecutorService loggerScheduler;

    private volatile DataReceiver currentDataReceiver = null;

    public AliClientRegistry(ICommonUtils utils) {
        this.utils = utils;
    }

    public CompletableFuture<Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>>> getCurrentDataFuture() {
        DataReceiver receiver = currentDataReceiver;

        if (receiver == null) {
            return CompletableFuture.completedFuture(Pair.of(Collections.emptyMap(), Collections.emptyMap()));
        }

        return receiver.getFuture();
    }

    public void addLootData(ResourceLocation resourceLocation, IDataNode node, List<ItemStack> items) {
        DataReceiver receiver = currentDataReceiver;

        if (receiver == null) {
            return;
        }

        lootItemMap.put(resourceLocation, items);
        lootNodeMap.put(resourceLocation, node);
        receivedMessages.incrementAndGet();
        receivedMessagesPerSecond.incrementAndGet();

        receiver.messageReceived();
    }

    public void addTradeData(ResourceLocation resourceLocation, IDataNode node, List<Item> inputs, List<Item> outputs) {
        DataReceiver receiver = currentDataReceiver;

        if (receiver == null) {
            return;
        }

        lootTradeMap.put(resourceLocation, node);
        tradeInputItemMap.put(resourceLocation, inputs);
        tradeOutputItemMap.put(resourceLocation, outputs);
        receivedMessages.incrementAndGet();
        receivedMessagesPerSecond.incrementAndGet();

        receiver.messageReceived();
    }

    public synchronized void startLootData(int totalMessages) {
        if (currentDataReceiver != null && !currentDataReceiver.getFuture().isDone()) {
            LOGGER.warn("Tried to start data reception while another operation is in progress.");
            return;
        }

        currentDataReceiver = new DataReceiver(totalMessages, lootNodeMap, lootTradeMap);

        lootNodeMap.clear();
        lootTradeMap.clear();
        lootItemMap.clear();
        startLogging();

        LOGGER.info("Started receiving loot data");
    }

    public synchronized void clearLootData() {
        if (currentDataReceiver != null) {
            currentDataReceiver.cancelOperation();
            currentDataReceiver = null;
        }

        lootNodeMap.clear();
        lootTradeMap.clear();
        lootItemMap.clear();

        LOGGER.info("Cleared Loot data");
    }

    public synchronized void doneLootData() {
        DataReceiver receiver = currentDataReceiver;

        if (receiver == null) {
            return;
        }

        receiver.forceDone();
        stopLogging();
        LOGGER.info("Finished receiving loot data [{}/{}]", lootNodeMap.size(), lootTradeMap.size());
    }

    @Override
    public void registerWidget(ResourceLocation id, IWidgetFactory factory) {
        widgetMap.put(id, factory);
    }

    @Override
    public <T extends IDataNode> void registerNode(ResourceLocation id, NodeFactory<T> nodeFactory) {
        nodeFactoryMap.put(id, nodeFactory);
    }

    @Override
    public List<IWidget> createWidgets(IWidgetUtils utils, List<IDataNode> entries, RelativeRect parent, int maxWidth) {
        int posX = 0, posY = 0;
        List<IWidget> widgets = new LinkedList<>();
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
    public <T extends IDataNode> NodeFactory<T> getNodeFactory(ResourceLocation id) {
        //noinspection unchecked
        NodeFactory<T> nodeFactory = (NodeFactory<T>) nodeFactoryMap.get(id);

        return Objects.requireNonNullElseGet(nodeFactory, () -> {
            throw new IllegalStateException(String.format("Failed to construct node - node {%s} was not registered!", id));
        });
    }

    @Override
    public List<ItemStack> getLootItems(ResourceLocation location) {
        return lootItemMap.getOrDefault(location, Collections.emptyList());
    }

    @Override
    public List<Item> getTradeInputItems(ResourceLocation location) {
        return tradeInputItemMap.getOrDefault(location, Collections.emptyList());
    }

    @Override
    public List<Item> getTradeOutputItems(ResourceLocation location) {
        return tradeOutputItemMap.getOrDefault(location, Collections.emptyList());
    }

    @Override
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        return utils.createEntities(type, level);
    }

    public void printRegistrationInfo() {
        LOGGER.info("Registered {} widgets", widgetMap.size());
        LOGGER.info("Registered {} node factories", nodeFactoryMap.size());
        LOGGER.info("Registered {} trade factories", lootTradeMap.size());
    }

    private void startLogging() {
        Runnable logTask = () -> {
            long count = receivedMessagesPerSecond.getAndSet(0);

            LOGGER.info("Received {} messages per second", count);
        };

        receivedMessages.set(0);
        receivedMessagesPerSecond.set(0);
        loggerScheduler = Executors.newSingleThreadScheduledExecutor();
        loggerScheduler.scheduleAtFixedRate(logTask, 1, 1, TimeUnit.SECONDS);
    }

    private void stopLogging() {
        if (loggerScheduler != null) {
            long count = receivedMessagesPerSecond.getAndSet(0);

            LOGGER.info("Received last {} messages", count);
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

        private final CompletableFuture<Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>>> dataFuture = new CompletableFuture<>();
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private final AtomicReference<ScheduledFuture<?>> timeoutHandleRef = new AtomicReference<>();
        private final Map<ResourceLocation, IDataNode> lootNodes;
        private final Map<ResourceLocation, IDataNode> tradeNodes;

        private final CountDownLatch completionLatch;

        public DataReceiver(int expectedMessageCount, Map<ResourceLocation, IDataNode> lootNodes, Map<ResourceLocation, IDataNode> tradeNodes) {
            this.completionLatch = new CountDownLatch(expectedMessageCount);
            this.lootNodes = lootNodes;
            this.tradeNodes = tradeNodes;

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

        public void messageReceived() {
            if (dataFuture.isDone()) {
                return;
            }

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
            dataFuture.complete(Pair.of(lootNodes, tradeNodes));
            shutdownScheduler();
        }

        private void resetInactivityTimeout() {
            Runnable timeoutTask = () -> {
                if (!dataFuture.isDone()) {
                    String msg = String.format("Data reception failed due to inactivity timeout (%ds). Expected %d, received %d.",
                            INACTIVITY_TIMEOUT_SECONDS,
                            (completionLatch.getCount() + lootNodes.size() + tradeNodes.size()),
                            (lootNodes.size() + tradeNodes.size()));

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

        public CompletableFuture<Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>>> getFuture() {
            return dataFuture;
        }
    }
}
