package com.yanny.aci.manager;

import com.mojang.logging.LogUtils;
import com.yanny.aci.api.*;
import com.yanny.aci.compatibility.DataReceiver;
import com.yanny.aci.tooltip.TooltipNodePalette;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public abstract class CoreClientRegistry<
        TConfig,
        TCommonUtils extends CoreCommonRegistry<TConfig>,
        TDataNode    extends ICoreDataNode<?>,
        TWidgetUtils extends ICoreWidgetUtils<?, ?, ?, ?>,
        TClientUtils extends ICoreClientUtils<?, ?, ?>
        >
        extends
        BaseRegistry
        implements
        ICoreClientUtils<TDataNode, TWidgetUtils, TClientUtils>,
        ICoreCommonUtils<TConfig>,
        ICoreClientRegistry<TDataNode, TWidgetUtils, TClientUtils> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ManagedRegistry<Identifier, IWidgetFactory<TDataNode, TWidgetUtils>> widgetMap = register("node widgets", false, HashMap::new, Identifier::toString, null);
    private final ManagedRegistry<Identifier, BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode>> dataNodeFactoryMap = register("data node factories", false, HashMap::new, Identifier::toString, null);

    protected final TCommonUtils commonUtils;

    private final TooltipNodePalette tooltipNodeCache = new TooltipNodePalette();

    private final AtomicInteger receivedChunks = new AtomicInteger(0);
    private final AtomicInteger receivedChunksPerSecond = new AtomicInteger(0);
    private ScheduledExecutorService loggerScheduler;
    private final AtomicBoolean loggedIn = new AtomicBoolean(false);

    private volatile DataReceiver currentDataReceiver = null;
    private final AtomicReference<CompletableFuture<byte[]>> activeDataPromise = new AtomicReference<>(new CompletableFuture<>());

    public CoreClientRegistry(TCommonUtils registry) {
        commonUtils = registry;
    }

    @NotNull
    public abstract IWidgetFactory<TDataNode, TWidgetUtils> getMissingWidgetFactory();

    @Override
    public final void registerWidget(Identifier id, IWidgetFactory<TDataNode, TWidgetUtils> factory) {
        widgetMap.put(id, factory);
    }

    @Override
    public final void registerDataNode(Identifier id, BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode> dataFactory) {
        dataNodeFactoryMap.put(id, dataFactory);
    }

    @NotNull
    @Override
    public final List<IWidget> createWidgets(TWidgetUtils utils, List<TDataNode> entries, RelativeRect parent, int maxWidth) {
        int posX = 0, posY = 0;
        List<IWidget> widgets = new ArrayList<>(entries.size());
        WidgetDirection lastDirection = null;

        for (TDataNode entry : entries) {
            IWidgetFactory<TDataNode, TWidgetUtils> widgetFactory = widgetMap.get(entry.getId()).orElse(getMissingWidgetFactory());
            IWidget widget = widgetFactory.create(utils, entry, new RelativeRect(0, 0, maxWidth, 0, parent), maxWidth);

            widgets.add(widget);
        }

        int w = 0;
        int h = 0;

        for (int i = 0; i < widgets.size(); i++) {
            IWidget widget = widgets.get(i);
            WidgetDirection direction = widget.getDirection();
            RelativeRect bounds = widget.getRect();

            if (lastDirection != null) {
                if (lastDirection == WidgetDirection.HORIZONTAL && direction == WidgetDirection.HORIZONTAL) {
                    if (parent.getX() + posX + bounds.getWidth() <= maxWidth) {
                        bounds.setOffset(posX, posY);
                        posX += bounds.getWidth();
                    } else {
                        posX = bounds.getWidth();
                        posY += widgets.get(i - 1).getRect().getHeight();
                        bounds.setOffset(0, posY);
                    }
                } else {
                    posX = 0;

                    if (direction != lastDirection) {
                        if (lastDirection == WidgetDirection.HORIZONTAL) {
                            posY += widgets.get(i - 1).getRect().getHeight() + IWidget.PADDING;
                        }

                        bounds.setOffset(posX, posY);
                    } else {
                        bounds.setOffset(posX, posY);
                    }

                    if (direction != WidgetDirection.HORIZONTAL) {
                        posY += bounds.getHeight() + IWidget.PADDING;
                    } else {
                        posX += bounds.getWidth();
                    }
                }
            } else {
                bounds.setOffset(posX, posY);

                if (direction == WidgetDirection.HORIZONTAL) {
                    posX += bounds.getWidth();
                } else {
                    posY += bounds.getHeight() + IWidget.PADDING;
                }
            }

            w = Math.max(w, bounds.getOffsetX() + bounds.getWidth());
            h = Math.max(h, bounds.getOffsetY() + bounds.getHeight());
            lastDirection = direction;
        }

        parent.setDimensions(w, h);
        return widgets;
    }

    @NotNull
    @Override
    public final BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode> getDataNodeFactory(Identifier id) {
        Optional<BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode>> dataFactory = dataNodeFactoryMap.get(id);

        return dataFactory.orElseThrow(() -> new IllegalStateException(String.format("Failed to construct data node - node {%s} was not registered!", id)));
    }

    @NotNull
    @Override
    public final TConfig getConfiguration() {
        return commonUtils.getConfiguration();
    }

    @NotNull
    @Override
    public TooltipNodePalette getTooltipCache() {
        return tooltipNodeCache;
    }

    @Nullable
    @Override
    public String getTranslationKey(int index) {
        return commonUtils.getDictionary().inverse().get(index);
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
        if (currentDataReceiver != null) {
            currentDataReceiver.cancelOperation();
        }

        currentDataReceiver = new DataReceiver(totalMessages);
        CompletableFuture<byte[]> currentPromise = activeDataPromise.get();

        if (currentPromise.isDone()) {
            currentPromise = new CompletableFuture<>();
            activeDataPromise.set(currentPromise);
        }

        final CompletableFuture<byte[]> promiseToComplete = currentPromise;
        currentDataReceiver.getFuture().whenComplete((data, throwable) -> {
            if (throwable != null) {
                promiseToComplete.completeExceptionally(throwable);
            } else {
                promiseToComplete.complete(data);
            }
        });

        startLogging();
        LOGGER.info("Started receiving data");
    }

    public synchronized void clearLootData() {
        if (currentDataReceiver != null) {
            currentDataReceiver.cancelOperation();
            currentDataReceiver = null;
            stopLogging(true);
        }

        activeDataPromise.set(new CompletableFuture<>());
        LOGGER.info("Cleared data");
    }

    public synchronized void reloadData() {
        // reload is called on login, causing clearing already received data
        if (loggedIn.get()) {
            LOGGER.info("Reloading data");
            clearLootData();
        }
    }

    public synchronized void loggingIn(boolean modAvailableOnServer) {
        LOGGER.info("Player login received");
        loggedIn.set(true);

        if (!modAvailableOnServer) {
            LOGGER.info("Mod is not present on the server. Completing sync with empty data.");
            CompletableFuture<byte[]> currentPromise = activeDataPromise.get();

            if (!currentPromise.isDone()) {
                currentPromise.complete(new byte[0]);
            }
        }
    }

    public synchronized void loggingOut() {
        LOGGER.info("Player logout received");

        CompletableFuture<byte[]> oldPromise = activeDataPromise.get();

        if (oldPromise != null && !oldPromise.isDone()) {
            oldPromise.cancel(true);
        }

        clearLootData();
        loggedIn.set(false);
    }

    public synchronized void doneLootData() {
        DataReceiver receiver = currentDataReceiver;

        if (receiver == null) {
            return;
        }

        receiver.forceDone();
        stopLogging(false);
        LOGGER.info("Finished receiving data");
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
}
