package com.yanny.ali.compatibility.common;

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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
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
    private static final Identifier TEXTURE_LOC = com.yanny.ali.Utils.modLoc("textures/gui/gui.png");
    private static final int WIDGET_SIZE = 36;
    private static final int DOTS_WIDTH = Minecraft.getInstance().font.width("...");
    private static final Set<EntityType<?>> BROKEN_ENTITY_RENDERERS = new HashSet<>(); // entity types whose renderer crashed, do not retry every frame

    public static void renderEntity(Entity entity, Rect bounds, int fullWidth, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (entity instanceof LivingEntity livingEntity) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE_LOC,
                    bounds.x(),
                    bounds.y(),
                    0,
                    WIDGET_SIZE,
                    bounds.width(),
                    bounds.height(),
                    WIDGET_SIZE,
                    WIDGET_SIZE,
                    256,
                    256
            );

            if (!BROKEN_ENTITY_RENDERERS.contains(entity.getType())) {
                try {
                    float screenMouseX = (float) (Minecraft.getInstance().mouseHandler.xpos() / Minecraft.getInstance().getWindow().getGuiScale());
                    float screenMouseY = (float) (Minecraft.getInstance().mouseHandler.ypos() / Minecraft.getInstance().getWindow().getGuiScale());
                    EntityDimensions dimensions = entity.getType().getDimensions();
                    renderEntityInInventoryFollowsMouse(
                            guiGraphics,
                            bounds.x() + 1,
                            bounds.y() + 1,
                            bounds.right() - 1,
                            bounds.bottom() - 1,
                            (int) (Math.min(20 / dimensions.height(), 20 / dimensions.width())),
                            0.0625F,
                            mouseX + screenMouseX - bounds.x(),
                            mouseY+ screenMouseY,
                            livingEntity
                    );
                } catch (Throwable e) {
                    BROKEN_ENTITY_RENDERERS.add(entity.getType());
                    LOGGER.warn("Failed to render entity {}, skipping it from now on: {}", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()), e.getMessage(), e);
                }
            }

            guiGraphics.pose().popMatrix();
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
    public static Pair<Map<Identifier, IDataNode>, Map<Identifier, IDataNode>> decompressLootData(IClientUtils utils, byte[] fullCompressedData, RegistryAccess registryAccess) {
        Map<Identifier, IDataNode> lootData = new HashMap<>();
        Map<Identifier, IDataNode> tradeData = new HashMap<>();

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

    public static void renderEntityInInventoryFollowsMouse(GuiGraphicsExtractor guiGraphics, int left, int top, int right, int bottom,
                                                           int size, float scale, float mouseX, float mouseY, LivingEntity entity) {
        float hCenter = (float)(left + right) / 2.0F;
        float vCenter = (float)(top + bottom) / 2.0F;
        float xRotation = (float)Math.atan((hCenter - mouseX) / 40.0F);
        float yRotation = (float)Math.atan((vCenter - mouseY) / 40.0F);
        float yBodyRot = entity.yBodyRot;
        float entityYRot = entity.getYRot();
        float entityXRot = entity.getXRot();
        float yHeadRotO = entity.yHeadRotO;
        float yHeadRot = entity.yHeadRot;
        Quaternionf rotateZ = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf rotateX = (new Quaternionf()).rotateX(yRotation * 20.0F * ((float)Math.PI / 180F));

        rotateZ.mul(rotateX);
        guiGraphics.enableScissor(left, top, right, bottom);
        entity.yBodyRot = 180.0F + xRotation * 20.0F;
        entity.setYRot(180.0F + xRotation * 40.0F);
        entity.setXRot(-yRotation * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        float entityScale = entity.getScale();
        float $$22 = scale * entityScale;
        int $$23 = Math.round(size / entityScale);
        int x = (int) guiGraphics.pose().m20();
        int y = (int) guiGraphics.pose().m21();
        InventoryScreen.extractEntityInInventoryFollowsMouse(guiGraphics, left + x, top + y, right + x, bottom + y, $$23, $$22, mouseX, mouseY, entity);

        entity.yBodyRot = yBodyRot;
        entity.setYRot(entityYRot);
        entity.setXRot(entityXRot);
        entity.yHeadRotO = yHeadRotO;
        entity.yHeadRot = yHeadRot;
        guiGraphics.disableScissor();
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
    public static void pruneHiddenItems(Map<Identifier, IDataNode> lootData, Predicate<ItemStack> isVisible) {
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
    public static void pruneHiddenTrades(Map<Identifier, IDataNode> tradeData, Predicate<ItemStack> isVisible) {
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
                                   QuadConsumer<IDataNode, Identifier, Block, List<ItemStack>> blockConsumer,
                                   QuadConsumer<IDataNode, Identifier, EntityType<?>, List<ItemStack>> entityConsumer,
                                   TriConsumer<IDataNode, Identifier, List<ItemStack>> gameplayConsumer,
                                   QuintConsumer<IDataNode, Identifier, VillagerProfession, List<ItemStack>, List<ItemStack>> traderConsumer,
                                   QuadConsumer<IDataNode, Identifier, List<ItemStack>, List<ItemStack>> wanderingTraderConsumer) {
        Pair<Map<Identifier, IDataNode>, Map<Identifier, IDataNode>> pair = GenericUtils.decompressLootData(clientRegistry, fullCompressedData, level.registryAccess());
        Map<Identifier, IDataNode> lootData = pair.getA();
        Map<Identifier, IDataNode> tradeData = pair.getB();

        pruneHiddenItems(lootData, isVisible);
        pruneHiddenTrades(tradeData, isVisible);

        // a loot table is claimed only after every block using it got its own entry - blocks sharing one table
        // (vanilla's dropsLike) must not hide each other; claimed tables are dropped before the gameplay pass
        Set<Identifier> claimedLootTables = new HashSet<>();
        Map<Identifier, Set<Item>> handledBlockItems = new HashMap<>();
        // entities keep the one-entry-per-table rule: entity entries are identified by their loot table, so a table
        // shared by two entity types has to produce a single entry, shown under the first of them
        Map<Identifier, List<EntityType<?>>> entityLootTables = new EntityLootTableResolver(clientRegistry, level).resolveAll(lootData.keySet());

        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier location = Utils.getLootTableKey(block);

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

        for (Map.Entry<Identifier, List<EntityType<?>>> entry : entityLootTables.entrySet()) {
            Identifier location = entry.getKey();

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

        for (Map.Entry<Identifier, IDataNode> entry : lootData.entrySet()) {
            gameplayConsumer.accept(entry.getValue(), entry.getKey(), collectItems(entry.getValue()));
        }

        lootData.clear();

        List<Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession>> entries = BuiltInRegistries.VILLAGER_PROFESSION.entrySet()
                .stream()
                .sorted(Comparator.comparing(a -> a.getKey().identifier().getPath()))
                .toList();

        for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : entries) {
            Identifier location = entry.getKey().identifier();
            IDataNode node = tradeData.get(location);

            if (node != null) {
                Pair<List<ItemStack>, List<ItemStack>> items = collectTradeItems(node);

                traderConsumer.accept(node, location, entry.getValue(), items.getA(), items.getB());
                tradeData.remove(location);
            }
        }

        for (Map.Entry<Identifier, IDataNode> entry : tradeData.entrySet()) {
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
            Optional<Holder.Reference<PoiType>> poiType;

            if (poi.size() == 1 && (poiType = BuiltInRegistries.POINT_OF_INTEREST_TYPE.get(poi.getFirst())).isPresent()) {
                return poiType.get().value().matchingStates().stream().map(BlockBehaviour.BlockStateBase::getBlock).collect(Collectors.toSet());
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
    public static Triplet<Component, Component, Rect> prepareGameplayTitle(Identifier location, int maxWidth) {
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

    private static void readLootData(IClientUtils utils, RegistryFriendlyByteBuf readerBuf, Map<Identifier, IDataNode> lootData) {
        int lootDataCount = readerBuf.readInt();

        for (int i = 0; i < lootDataCount; i++) {
            Identifier location = readerBuf.readIdentifier();

            lootData.put(location, utils.getDataNodeFactory(LootTableNode.ID).apply(utils, readerBuf));
        }
    }

    private static void readTradeData(IClientUtils utils, RegistryFriendlyByteBuf buf, Map<Identifier, IDataNode> tradeData) {
        int tradeDataCount = buf.readInt();

        for (int i = 0; i < tradeDataCount; i++) {
            Identifier location = buf.readIdentifier();

            tradeData.put(location, utils.getDataNodeFactory(TradeNode.ID).apply(utils, buf));
        }

        // wandering trader
        tradeData.put(Identifier.withDefaultNamespace("empty"), utils.getDataNodeFactory(TradeNode.ID).apply(utils, buf));
    }
}
