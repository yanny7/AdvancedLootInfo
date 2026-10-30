package com.yanny.aci.manager;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.*;
import com.yanny.aci.compatibility.DataReceiver;
import com.yanny.aci.tooltip.TooltipNodePalette;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

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

    private static final int MAX_REQUESTS = 3;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    private final Logger logger;

    private final ManagedRegistry<ResourceLocation, IWidgetFactory<TDataNode, TWidgetUtils>> widgetMap = register("node widgets", false, HashMap::new, ResourceLocation::toString, null);
    private final ManagedRegistry<ResourceLocation, BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode>> dataNodeFactoryMap = register("data node factories", false, HashMap::new, ResourceLocation::toString, null);

    protected final TCommonUtils commonUtils;

    private final TooltipNodePalette tooltipNodeCache;

    private final AtomicInteger receivedChunks = new AtomicInteger(0);
    private final AtomicInteger receivedChunksPerSecond = new AtomicInteger(0);
    private ScheduledExecutorService loggerScheduler;
    private final AtomicBoolean loggedIn = new AtomicBoolean(false);
    private volatile boolean modAvailableOnServer = false;

    private volatile DataReceiver currentDataReceiver = null;
    private final AtomicReference<CompletableFuture<byte[]>> activeDataPromise = new AtomicReference<>(new CompletableFuture<>());

    public CoreClientRegistry(TCommonUtils registry) {
        super(registry.getModId());
        logger = CommonLogUtils.getLogger(registry.getModId());
        tooltipNodeCache = new TooltipNodePalette(registry.getModId());
        commonUtils = registry;
    }

    @NotNull
    public abstract IWidgetFactory<TDataNode, TWidgetUtils> getMissingWidgetFactory();

    @Override
    public final void registerWidget(ResourceLocation id, IWidgetFactory<TDataNode, TWidgetUtils> factory) {
        widgetMap.put(id, factory);
    }

    @Override
    public final void registerDataNode(ResourceLocation id, BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode> dataFactory) {
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
    public final BiFunction<TClientUtils, RegistryFriendlyByteBuf, TDataNode> getDataNodeFactory(ResourceLocation id) {
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

    public final <T> void registerData(T registry, BiConsumer<T, byte[]> registerData, BooleanSupplier canRequestData, Runnable requestData) {
        logger.info("Starting data registration...");

        try {
            byte[] data = awaitData(canRequestData, requestData);

            if (data != null) {
                registerData.accept(registry, data);
                logger.info("Data registration finished successfully.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Registration thread interrupted!", e);
        } catch (ExecutionException e) {
            logger.error("Failed to finish registering data with error", e);
        } catch (Throwable e) {
            logger.error("Failed to finish registering data with unexpected error", e);
        }
    }

    public void addChunkData(int index, byte[] data) {
        DataReceiver receiver = currentDataReceiver;

        if (receiver == null) {
            logger.warn("Dropping data chunk {} - no active receiver (StartMessage was never received or data was cleared)", index);
            return;
        }

        receivedChunks.incrementAndGet();
        receivedChunksPerSecond.incrementAndGet();
        receiver.messageReceived(index, data);
    }

    public synchronized void startReceivingData(int totalMessages) {
        if (currentDataReceiver != null) {
            currentDataReceiver.cancelOperation();
            stopLogging(true);
        }

        currentDataReceiver = new DataReceiver(getModId(), totalMessages);
        CompletableFuture<byte[]> currentPromise = activeDataPromise.get();

        if (currentPromise.isDone()) {
            currentPromise = new CompletableFuture<>();
            activeDataPromise.set(currentPromise);
        }

        final CompletableFuture<byte[]> promiseToComplete = currentPromise;
        currentDataReceiver.getFuture().whenComplete((data, throwable) -> {
            if (throwable instanceof CancellationException) {
                return;
            }

            if (throwable != null) {
                promiseToComplete.completeExceptionally(throwable);
            } else {
                promiseToComplete.complete(data);
            }
        });

        startLogging();
        logger.info("Started receiving data");
    }

    public synchronized void clearReceivedData() {
        if (currentDataReceiver != null) {
            currentDataReceiver.cancelOperation();
            currentDataReceiver = null;
            stopLogging(true);
        }

        activeDataPromise.getAndSet(new CompletableFuture<>()).cancel(false);
        logger.info("Cleared data");
    }

    public synchronized void reloadData() {
        if (loggedIn.get() && modAvailableOnServer) {
            logger.info("Reloading data");
            clearReceivedData();
        }
    }

    public synchronized void loggingIn(boolean modAvailableOnServer) {
        logger.info("Player login received");
        loggedIn.set(true);
        this.modAvailableOnServer = modAvailableOnServer;

        if (!modAvailableOnServer) {
            logger.info("Mod is not present on the server. Completing sync with empty data.");
            CompletableFuture<byte[]> currentPromise = activeDataPromise.get();

            if (!currentPromise.isDone()) {
                currentPromise.complete(new byte[0]);
            }
        }
    }

    public synchronized void loggingOut() {
        logger.info("Player logout received");
        loggedIn.set(false);
        clearReceivedData();
    }

    public synchronized void doneReceivingData() {
        DataReceiver receiver = currentDataReceiver;

        if (receiver == null) {
            return;
        }

        receiver.forceDone();
        stopLogging(false);
        logger.info("Finished receiving data");
    }

    private byte @Nullable[] awaitData(BooleanSupplier canRequestData, Runnable requestData) throws InterruptedException, ExecutionException {
        attempts:
        for (int attempt = 1; attempt <= MAX_REQUESTS; attempt++) {
            CompletableFuture<byte[]> promise = activeDataPromise.get();

            if (promise.isDone()) {
                logger.info("Data already received, processing instantly.");
                return promise.get();
            }

            if (isDisconnected(canRequestData)) {
                return null;
            }

            logger.info("Data not ready. Requesting data from server (Attempt {}/{})", attempt, MAX_REQUESTS);
            requestData.run();

            long deadline = System.nanoTime() + REQUEST_TIMEOUT.toNanos();

            while (System.nanoTime() < deadline) {
                try {
                    return promise.get(POLL_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
                } catch (TimeoutException ignored) {
                } catch (CancellationException e) {
                    logger.info("Data was cleared while waiting for it");
                    continue attempts;
                }

                if (isDisconnected(canRequestData)) {
                    return null;
                }
            }

            logger.warn("Timeout while waiting for server data! The server didn't respond in time or packets were lost.");
            clearReceivedData();
        }

        logger.error("CRITICAL: Could not fetch data from server after {} attempts. Recipe viewer integration will be empty or incomplete.", MAX_REQUESTS);
        return null;
    }

    private boolean isDisconnected(BooleanSupplier canRequestData) {
        if (loggedIn.get() && canRequestData.getAsBoolean()) {
            return false;
        }

        logger.info("Not connected to the server, data registration skipped");
        return true;
    }

    private void startLogging() {
        Runnable logTask = () -> {
            long count = receivedChunksPerSecond.getAndSet(0);

            logger.info("Received {} chunk(s) per second", count);
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
                logger.info("Received last {} chunk(s). Done receiving data.", count);
            }

            loggerScheduler.shutdownNow();

            try {
                if (!loggerScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("Logging scheduler didn't stop in time!");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
