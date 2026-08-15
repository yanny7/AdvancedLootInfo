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
import com.yanny.ali.api.ITradeNode;
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
    public static Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>> decompressLootData(IClientUtils utils, byte[] fullCompressedData, RegistryAccess registryAccess) {
        Map<ResourceLocation, IDataNode> lootData = new HashMap<>();
        Map<ResourceLocation, IDataNode> tradeData = new HashMap<>();

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
     * that, and drops loot tables left with nothing at all. Must run before a tree is handed to a recipe/widget and
     * before {@link #collectItems} is asked for the recipe's outputs, so that the rendered tree, the outputs and the
     * reverse index cannot disagree on what is visible.
     * <p>
     * A tag entry loses the members the viewer hides rather than being kept whole, and is dropped once none are left -
     * see {@link #keepItemNode}.
     *
     * @param isVisible viewer-specific visibility test
     */
    public static void pruneHiddenItems(Map<ResourceLocation, IDataNode> lootData, Predicate<ItemStack> isVisible) {
        lootData.values().removeIf((node) -> node instanceof ListNode listNode && listNode.prune(hiddenItemFilter(isVisible)));
    }

    /**
     * Same as {@link #pruneHiddenItems}, for villager trades. A single trade ({@code ItemsToItemsNode}) is all or
     * nothing - it disappears as soon as any of its inputs or its result is hidden, because a trade shown without one
     * of its costs reads as a different, cheaper trade. Trade levels and professions left empty by that are dropped
     * too. See {@code CoreListNode#requiresAllChildren}.
     * <p>
     * The flat input/output lists a viewer indexes are read back out of the pruned tree afterwards
     * ({@link #collectTradeItems}), so an item that only occurred in a dropped trade leaves the index with it.
     */
    public static void pruneHiddenTrades(Map<ResourceLocation, IDataNode> tradeData, Predicate<ItemStack> isVisible) {
        tradeData.values().removeIf((node) -> node instanceof ListNode listNode && listNode.prune(hiddenItemFilter(isVisible)));
    }

    @NotNull
    private static Predicate<ICoreDataNode<?>> hiddenItemFilter(Predicate<ItemStack> isVisible) {
        return (node) -> !(node instanceof IItemNode itemNode) || keepItemNode(itemNode, isVisible);
    }

    /**
     * A node survives while it still stands for something the player can see. The hidden members of a tag are dropped
     * first, so that {@link IItemNode#getItems()} - what the recipe outputs and the reverse index are built from -
     * cannot claim more than the tree shows; a tag left without a single visible member is dropped whole.
     * <p>
     * A node standing for nothing at all is kept: the second input of a single-input villager trade is an empty
     * placeholder stack, and dropping it would take the whole trade with it (see {@code requiresAllChildren}).
     */
    private static boolean keepItemNode(IItemNode itemNode, Predicate<ItemStack> isVisible) {
        itemNode.retainItems(isVisible);

        List<ItemStack> items = itemNode.getItems();

        if (items.isEmpty()) {
            return itemNode.getItem().left().filter(ItemStack::isEmpty).isPresent();
        }

        return items.stream().anyMatch(isVisible);
    }

    /** Every stack the tree below {@code node} stands for - the client-side counterpart of the server's own walk. */
    @NotNull
    public static List<ItemStack> collectItems(IDataNode node) {
        List<ItemStack> items = new ArrayList<>();

        if (node instanceof ListNode listNode) {
            for (IDataNode child : listNode.nodes()) {
                items.addAll(collectItems(child));
            }
        } else if (node instanceof IItemNode itemNode) {
            items.addAll(itemNode.getItems());
        }

        return items;
    }

    /**
     * The costs and the results of every trade below {@code node}, kept apart - only an {@link ITradeNode} knows which
     * of its children is which, so the walk asks it instead of looking at the item nodes itself.
     */
    @NotNull
    public static Pair<List<ItemStack>, List<ItemStack>> collectTradeItems(IDataNode node) {
        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();

        collectTradeItems(node, inputs, outputs);
        return new Pair<>(inputs, outputs);
    }

    private static void collectTradeItems(IDataNode node, List<ItemStack> inputs, List<ItemStack> outputs) {
        if (node instanceof ITradeNode tradeNode) {
            inputs.addAll(tradeNode.getInputItems());
            outputs.addAll(tradeNode.getOutputItems());
        } else if (node instanceof ListNode listNode) {
            for (IDataNode child : listNode.nodes()) {
                collectTradeItems(child, inputs, outputs);
            }
        }
    }

    public static void processData(ClientLevel level, AliClientRegistry clientRegistry, AliConfig config, byte[] fullCompressedData,
                                   Predicate<ItemStack> isVisible,
                                   QuadConsumer<IDataNode, ResourceLocation, Block, List<ItemStack>> blockConsumer,
                                   QuadConsumer<IDataNode, ResourceLocation, EntityType<?>, List<ItemStack>> entityConsumer,
                                   TriConsumer<IDataNode, ResourceLocation, List<ItemStack>> gameplayConsumer,
                                   QuintConsumer<IDataNode, ResourceLocation, VillagerProfession, List<ItemStack>, List<ItemStack>> traderConsumer,
                                   QuadConsumer<IDataNode, ResourceLocation, List<ItemStack>, List<ItemStack>> wanderingTraderConsumer) {
        Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>> pair = GenericUtils.decompressLootData(clientRegistry, fullCompressedData, level.registryAccess());
        Map<ResourceLocation, IDataNode> lootData = pair.getA();
        Map<ResourceLocation, IDataNode> tradeData = pair.getB();

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
                IDataNode node = lootData.get(location);

                if (node != null) {
                    // blocks sharing both a loot table and an item (StandingAndWallBlockItem, e.g. banner + wall
                    // banner) would render as two identical entries; blocks with no item of their own are drawn as
                    // the block itself, so those stay distinguishable and are always kept
                    Item item = block.asItem();

                    if (item == Items.AIR || handledBlockItems.computeIfAbsent(location, (k) -> new HashSet<>()).add(item)) {
                        blockConsumer.accept(node, location, block, collectItems(node));
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

            IDataNode node = lootData.get(location);

            if (node != null) {
                entityConsumer.accept(node, location, entry.getValue().get(0), collectItems(node));
            }

            claimedLootTables.add(location);
        }

        lootData.keySet().removeAll(claimedLootTables);

        for (Map.Entry<ResourceLocation, IDataNode> entry : lootData.entrySet()) {
            gameplayConsumer.accept(entry.getValue(), entry.getKey(), collectItems(entry.getValue()));
        }

        lootData.clear();

        List<Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession>> entries = BuiltInRegistries.VILLAGER_PROFESSION.entrySet()
                .stream()
                .sorted(Comparator.comparing(a -> a.getKey().location().getPath()))
                .toList();

        for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : entries) {
            ResourceLocation location = entry.getKey().location();
            IDataNode node = tradeData.get(location);

            if (node != null) {
                Pair<List<ItemStack>, List<ItemStack>> items = collectTradeItems(node);

                traderConsumer.accept(node, location, entry.getValue(), items.getA(), items.getB());
                tradeData.remove(location);
            }
        }

        for (Map.Entry<ResourceLocation, IDataNode> entry : tradeData.entrySet()) {
            Pair<List<ItemStack>, List<ItemStack>> items = collectTradeItems(entry.getValue());

            wanderingTraderConsumer.accept(entry.getValue(), entry.getKey(), items.getA(), items.getB());
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

    private static void readLootData(IClientUtils utils, RegistryFriendlyByteBuf readerBuf, Map<ResourceLocation, IDataNode> lootData) {
        int lootDataCount = readerBuf.readInt();

        for (int i = 0; i < lootDataCount; i++) {
            ResourceLocation location = readerBuf.readResourceLocation();

            lootData.put(location, utils.getDataNodeFactory(LootTableNode.ID).apply(utils, readerBuf));
        }
    }

    private static void readTradeData(IClientUtils utils, RegistryFriendlyByteBuf buf, Map<ResourceLocation, IDataNode> tradeData) {
        int tradeDataCount = buf.readInt();

        for (int i = 0; i < tradeDataCount; i++) {
            ResourceLocation location = buf.readResourceLocation();

            tradeData.put(location, utils.getDataNodeFactory(TradeNode.ID).apply(utils, buf));
        }

        // wandering trader
        tradeData.put(ResourceLocation.withDefaultNamespace("empty"), utils.getDataNodeFactory(TradeNode.ID).apply(utils, buf));
    }
}
