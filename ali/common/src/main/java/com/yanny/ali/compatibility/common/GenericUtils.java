package com.yanny.ali.compatibility.common;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.yanny.aci.api.ICoreDataNode;
import com.yanny.aci.api.Rect;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.RequestLootDataMessage;
import com.yanny.ali.plugin.common.EntityLootTableResolver;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import com.yanny.ali.plugin.mods.PluginUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class GenericUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TEXTURE_LOC = com.yanny.ali.Utils.modLoc("textures/gui/gui.png");
    private static final int WIDGET_SIZE = 36;
    private static final int DOTS_WIDTH = Minecraft.getInstance().font.width("...");
    private static final Set<EntityType<?>> BROKEN_ENTITY_RENDERERS = new HashSet<>(); // entity types whose renderer crashed, do not retry every frame

    public static void renderEntity(Entity entity, Rect bounds, int fullWidth, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        PoseStack poseStack = guiGraphics.pose();

        // Get the model-view matrix (combined) from the PoseStack
        Matrix4f modelViewMatrix = new Matrix4f(poseStack.last().pose());
        // Get the projection matrix
        Matrix4f projectionMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());
        // Combine model-view and projection
        Matrix4f mvpMatrix = projectionMatrix.mul(modelViewMatrix);
        // Define the 3D coordinates of the top-left and bottom-right corners of your element
        // Since it's a 2D element in GUI, Z can be 0.
        Vector4f topLeftWorld = new Vector4f(0, 0, 0, 1);
        // Project to clip space
        Vector4f topLeftClip = mvpMatrix.transform(topLeftWorld);
        // Perspective divide
        Vector4f topLeftNDC = new Vector4f(topLeftClip.x / topLeftClip.w, topLeftClip.y / topLeftClip.w, 0, 1);

        // Convert to screen coordinates (pixels)
        int screenX = Math.round((topLeftNDC.x + 1) / 2f * window.getGuiScaledWidth());
        int screenY = Math.round((1 - topLeftNDC.y) / 2f * window.getGuiScaledHeight());

        if (entity instanceof LivingEntity livingEntity) {
            guiGraphics.pose().pushPose();
            guiGraphics.blit(
                    TEXTURE_LOC,
                    bounds.x(),
                    bounds.y(),
                    bounds.width(),
                    bounds.height(),
                    0,
                    36,
                    36,
                    36,
                    256,
                    256
            );

            if (!BROKEN_ENTITY_RENDERERS.contains(entity.getType())) {
                guiGraphics.enableScissor(screenX + bounds.x() + 1, screenY + bounds.y() + 1, screenX + bounds.right() - 1, screenY + bounds.bottom() - 1);

                try {
                    EntityDimensions dimensions = entity.getType().getDimensions();
                    InventoryScreen.renderEntityInInventoryFollowsMouse(
                            guiGraphics,
                            -screenX + bounds.x(),
                            -screenY + bounds.y(),
                            screenX + bounds.right(),
                            screenY + bounds.bottom(),
                            (int) (Math.min(20 / dimensions.height(), 20 / dimensions.width())),
                            0.0625F,
                            mouseX,
                            mouseY,
                            livingEntity
                    );
                } catch (Throwable e) {
                    BROKEN_ENTITY_RENDERERS.add(entity.getType());
                    LOGGER.warn("Failed to render entity {}, skipping it from now on: {}", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()), e.getMessage(), e);

                    // InventoryScreen#renderEntityInInventory pushed the pose and changed the render state, but never restored it
                    guiGraphics.pose().popPose();
                    minecraft.getEntityRenderDispatcher().setRenderShadow(true);
                    Lighting.setupFor3DItems();
                }

                guiGraphics.disableScissor();
            }

            guiGraphics.pose().popPose();
        }
    }

    @NotNull
    public static Component ellipsis(String text, String fallback, int maxWidth) {
        Font font = Minecraft.getInstance().font;

        text = Language.getInstance().getOrDefault(text, getFallbackText(fallback));

        if (font.width(text) > maxWidth) {
            int index = 20;

            while (font.width(text.substring(0, index + 1) + DOTS_WIDTH) <= maxWidth) {
                index += 1;
            }

            return Component.literal(text.substring(0, index) + "...");
        }

        return Component.literal(text);
    }

    @NotNull
    public static Pair<Map<ResourceLocation, LootData>, Map<ResourceLocation, TradeData>> decompressLootData(IClientUtils utils, byte[] fullCompressedData, RegistryAccess registryAccess) {
        Map<ResourceLocation, LootData> lootData = new HashMap<>();
        Map<ResourceLocation, TradeData> tradeData = new HashMap<>();

        if (fullCompressedData.length == 0) {
            return new Pair<>(lootData, tradeData);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(fullCompressedData);
        ByteBuf decompressedBuf = Unpooled.buffer();

        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            decompressedBuf.writeBytes(gzip.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(decompressedBuf, registryAccess);

        try {
            utils.getTooltipCache().decode(utils, buf);
            readLootData(utils, buf, lootData);
            readTradeData(utils, buf, tradeData);
        } catch (Throwable e) {
            LOGGER.warn("Failed to decode loot data!", e);
        } finally {
            buf.release();
        }

        return new Pair<>(lootData, tradeData);
    }

    /**
     * Drops every item the recipe viewer hides from the decoded loot trees, along with any pool/group left empty by
     * that, and drops loot tables left with nothing at all. Must run before a tree is handed to a recipe/widget, so
     * that the rendered tree and the recipe outputs agree on what is visible.
     * <p>
     * Tag entries are always kept - a tag is not a stack the viewer can hide, and resolving it here would change what
     * the tooltip claims the loot table contains.
     *
     * @param isVisible viewer-specific visibility test
     */
    public static void pruneHiddenItems(Map<ResourceLocation, LootData> lootData, Predicate<ItemStack> isVisible) {
        Iterator<Map.Entry<ResourceLocation, LootData>> iterator = lootData.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, LootData> entry = iterator.next();
            LootData data = entry.getValue();

            if (data.node() instanceof ListNode listNode && listNode.prune(hiddenItemFilter(isVisible))) {
                iterator.remove();
            } else {
                entry.setValue(new LootData(data.node(), data.items().stream().filter(isVisible).toList()));
            }
        }
    }

    /**
     * Same as {@link #pruneHiddenItems}, for villager trades. A single trade ({@code ItemsToItemsNode}) is all or
     * nothing - it disappears as soon as any of its inputs or its result is hidden, because a trade shown without one
     * of its costs reads as a different, cheaper trade. Trade levels and professions left empty by that are dropped
     * too. See {@code CoreListNode#requiresAllChildren}.
     * <p>
     * The flat input/output lists are filtered per item rather than recomputed from the pruned tree: they only feed
     * the viewer's ingredient index, and the tree does not record which side of a trade an item sat on. An item that
     * is visible itself but only occurred in a dropped trade therefore stays in the index.
     */
    public static void pruneHiddenTrades(Map<ResourceLocation, TradeData> tradeData, Predicate<ItemStack> isVisible) {
        Iterator<Map.Entry<ResourceLocation, TradeData>> iterator = tradeData.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, TradeData> entry = iterator.next();
            TradeData data = entry.getValue();

            if (data.node() instanceof ListNode listNode && listNode.prune(hiddenItemFilter(isVisible))) {
                iterator.remove();
            } else {
                entry.setValue(new TradeData(data.node(),
                        data.inputs().stream().filter((item) -> isVisible.test(item.getDefaultInstance())).toList(),
                        data.outputs().stream().filter((item) -> isVisible.test(item.getDefaultInstance())).toList()));
            }
        }
    }

    @NotNull
    private static Predicate<ICoreDataNode<?>> hiddenItemFilter(Predicate<ItemStack> isVisible) {
        return (node) -> !(node instanceof IItemNode itemNode)
                || itemNode.getModifiedItem().map(isVisible::test, (tag) -> true);
    }

    public static void processData(ClientLevel level, AliClientRegistry clientRegistry, AliConfig config, byte[] fullCompressedData,
                                   Predicate<ItemStack> isVisible,
                                   QuadConsumer<IDataNode, ResourceLocation, Block, List<ItemStack>> blockConsumer,
                                   QuadConsumer<IDataNode, ResourceLocation, EntityType<?>, List<ItemStack>> entityConsumer,
                                   TriConsumer<IDataNode, ResourceLocation, List<ItemStack>> gameplayConsumer,
                                   QuintConsumer<IDataNode, ResourceLocation, VillagerProfession, List<ItemStack>, List<ItemStack>> traderConsumer,
                                   QuadConsumer<IDataNode, ResourceLocation, List<ItemStack>, List<ItemStack>> wanderingTraderConsumer) {
        Pair<Map<ResourceLocation, LootData>, Map<ResourceLocation, TradeData>> pair = GenericUtils.decompressLootData(clientRegistry, fullCompressedData, level.registryAccess());
        Map<ResourceLocation, LootData> lootData = pair.getA();
        Map<ResourceLocation, TradeData> tradeData = pair.getB();

        pruneHiddenItems(lootData, isVisible);
        pruneHiddenTrades(tradeData, isVisible);

        // a loot table is claimed only after every block using it got its own entry - blocks sharing one table
        // (vanilla's dropsLike) must not hide each other; claimed tables are dropped before the gameplay pass
        Set<ResourceLocation> claimedLootTables = new HashSet<>();
        Map<ResourceLocation, Set<Item>> handledBlockItems = new HashMap<>();
        // entities keep the one-entry-per-table rule: entity entries are identified by their loot table, so a table
        // shared by two entity types has to produce a single entry, shown under the first of them
        Map<ResourceLocation, List<EntityType<?>>> entityLootTables = new EntityLootTableResolver(clientRegistry, level).resolveAll(lootData.keySet());

        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation location = Utils.getLootTableKey(block);

            //noinspection ConstantValue
            if (location != null) {
                LootData data = lootData.get(location);

                if (data != null) {
                    // blocks sharing both a loot table and an item (StandingAndWallBlockItem, e.g. banner + wall
                    // banner) would render as two identical entries; blocks with no item of their own are drawn as
                    // the block itself, so those stay distinguishable and are always kept
                    Item item = block.asItem();

                    if (item == Items.AIR || handledBlockItems.computeIfAbsent(location, (k) -> new HashSet<>()).add(item)) {
                        blockConsumer.accept(data.node, location, block, data.items);
                    }

                    claimedLootTables.add(location);
                }
            }
        }

        for (Map.Entry<ResourceLocation, List<EntityType<?>>> entry : entityLootTables.entrySet()) {
            ResourceLocation location = entry.getKey();

            if (entry.getValue().stream().allMatch((t) -> config.disabledEntities.stream().anyMatch((f) -> f.equals(BuiltInRegistries.ENTITY_TYPE.getKey(t))))) {
                claimedLootTables.add(location);
                continue;
            }

            LootData data = lootData.get(location);

            if (data != null) {
                entityConsumer.accept(data.node, location, entry.getValue().get(0), data.items);
            }

            claimedLootTables.add(location);
        }

        lootData.keySet().removeAll(claimedLootTables);

        for (Map.Entry<ResourceLocation, LootData> entry : lootData.entrySet()) {
            gameplayConsumer.accept(entry.getValue().node, entry.getKey(), entry.getValue().items());
        }

        lootData.clear();

        List<Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession>> entries = BuiltInRegistries.VILLAGER_PROFESSION.entrySet()
                .stream()
                .sorted(Comparator.comparing(a -> a.getKey().location().getPath()))
                .toList();

        for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : entries) {
            ResourceLocation location = entry.getKey().location();
            TradeData tradeEntry = tradeData.get(location);

            if (tradeEntry != null) {
                List<ItemStack> inputs = tradeEntry.inputs.stream().map(Item::getDefaultInstance).toList();
                List<ItemStack> outputs = tradeEntry.outputs.stream().map(Item::getDefaultInstance).toList();

                traderConsumer.accept(tradeEntry.node, location, entry.getValue(), inputs, outputs);
                tradeData.remove(location);
            }
        }

        for (Map.Entry<ResourceLocation, TradeData> entry : tradeData.entrySet()) {
            ResourceLocation location = entry.getKey();
            TradeData tradeEntry = tradeData.get(location);

            if (tradeEntry != null) {
                List<ItemStack> inputs = tradeEntry.inputs.stream().map(Item::getDefaultInstance).toList();
                List<ItemStack> outputs = tradeEntry.outputs.stream().map(Item::getDefaultInstance).toList();

                wanderingTraderConsumer.accept(tradeEntry.node, location, inputs, outputs);
            }
        }

        tradeData.clear();
    }

    public static <T> void register(T emiRegistry, BiConsumer<T, byte[]> registerData) {
        LOGGER.info("Starting data registration...");
        int maxRetries = 3;
        int currentTry = 0;

        while (currentTry < maxRetries) {
            currentTry++;
            CompletableFuture<byte[]> futureData = PluginManager.getInstance().clientRegistry.getCurrentDataFuture();

            if (!futureData.isDone()) {
                LOGGER.info("Data not ready. Requesting data from server (Attempt {}/{})", currentTry, maxRetries);
                AbstractClient.INSTANCE.sendLootDataToPlayer(new RequestLootDataMessage());
            } else {
                LOGGER.info("Data already received, processing instantly.");
            }

            try {
                byte[] fullCompressedData = futureData.get(30, TimeUnit.SECONDS);

                registerData.accept(emiRegistry, fullCompressedData);
                LOGGER.info("Data registration finished successfully.");
                return;
            } catch (TimeoutException e) {
                LOGGER.warn("Timeout while waiting for server data! The server didn't respond in time or packets were lost.", e);
                PluginManager.getInstance().clientRegistry.clearReceivedData();
            } catch (CancellationException e) {
                LOGGER.warn("Data reception was cancelled. Retrying with new data stream...", e);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Registration thread interrupted!", e);
                return;
            } catch (ExecutionException e) {
                LOGGER.error("Failed to finish registering data with error", e);
                return;
            } catch (Throwable e) {
                LOGGER.error("Failed to finish registering data with unexpected error", e);
                return;
            }
        }

        LOGGER.error("CRITICAL: Could not fetch loot data from server after {} attempts. Recipe viewer integration will be empty or incomplete.", maxRetries);
    }

    public static Set<Block> getJobSites(@Nullable VillagerProfession profession) {
        if (profession != null) {
            //noinspection unchecked
            List<ResourceKey<PoiType>> poi = (List<ResourceKey<PoiType>>) (Object) PluginUtils.getCapturedInstances(profession.acquirableJobSite(), ResourceKey.class);
            PoiType poiType;

            if (poi.size() == 1 && (poiType = BuiltInRegistries.POINT_OF_INTEREST_TYPE.get(poi.getFirst())) != null) {
                return poiType.matchingStates().stream().map(BlockBehaviour.BlockStateBase::getBlock).collect(Collectors.toSet());
            }
        }

        return Set.of();
    }

    public static Set<Item> getRequestedItems(@Nullable VillagerProfession profession) {
        if (profession != null) {
            return profession.requestedItems();
        }

        return Set.of();
    }

    @NotNull
    public static Triplet<Component, Component, Rect> prepareGameplayTitle(ResourceLocation location, int maxWidth) {
        String key = "ali/loot_table/" + location.getPath();
        Component text = GenericUtils.ellipsis(key, location.getPath(), maxWidth);
        Component fullText = Component.literal(location.toString());
        Rect rect = new Rect(0, 0, Minecraft.getInstance().font.width(text), 8);

        return new Triplet<>(text, fullText, rect);
    }

    @NotNull
    public static Triplet<Component, Component, Rect> prepareTraderTitle(String path, int maxWidth) {
        String key = path.equals("empty") ? "entity.minecraft.wandering_trader" : "entity.minecraft.villager." + path;
        String id = path.equals("empty") ? "wandering_trader" : path;
        Component text = GenericUtils.ellipsis(key, id, maxWidth);
        Component fullText = Component.translatableWithFallback(key, id);
        Rect rect = new Rect(0, 0, Minecraft.getInstance().font.width(text), 8);

        return new Triplet<>(text, fullText, rect);
    }

    // split table path and make uppercased text
    private static String getFallbackText(String fallback) {
        List<String> pathSegments = Pattern.compile("/").splitAsStream(fallback).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        Collections.reverse(pathSegments);

        return pathSegments.stream()
                .flatMap(segment -> Arrays.stream(segment.split("_")))
                .filter(s -> !s.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static void readLootData(IClientUtils utils, RegistryFriendlyByteBuf readerBuf, Map<ResourceLocation, LootData> lootData) {
        int lootDataCount = readerBuf.readInt();

        for (int i = 0; i < lootDataCount; i++) {
            ResourceLocation location = readerBuf.readResourceLocation();
            IDataNode dataNode = utils.getDataNodeFactory(LootTableNode.ID).apply(utils, readerBuf);
            List<ItemStack> items = ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(readerBuf);

            lootData.put(location, new LootData(dataNode, items));
        }
    }

    private static void readTradeData(IClientUtils utils, RegistryFriendlyByteBuf buf, Map<ResourceLocation, TradeData> tradeData) {
        int tradeDataCount = buf.readInt();

        for (int i = 0; i < tradeDataCount; i++) {
            ResourceLocation location = buf.readResourceLocation();
            IDataNode dataNode = utils.getDataNodeFactory(TradeNode.ID).apply(utils, buf);
            List<Item> inputs = buf.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation).stream().map(BuiltInRegistries.ITEM::get).toList();
            List<Item> outputs = buf.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation).stream().map(BuiltInRegistries.ITEM::get).toList();
            tradeData.put(location, new TradeData(dataNode, inputs, outputs));
        }

        // wandering trader
        IDataNode dataNode = utils.getDataNodeFactory(TradeNode.ID).apply(utils, buf);
        List<Item> inputs = buf.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation).stream().map(BuiltInRegistries.ITEM::get).toList();
        List<Item> outputs = buf.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation).stream().map(BuiltInRegistries.ITEM::get).toList();

        tradeData.put(ResourceLocation.withDefaultNamespace("empty"), new TradeData(dataNode, inputs, outputs));
    }

    public record LootData(IDataNode node, List<ItemStack> items) {}

    public record TradeData(IDataNode node, List<Item> inputs, List<Item> outputs) {}
}
