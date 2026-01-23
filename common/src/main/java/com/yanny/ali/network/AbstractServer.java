package com.yanny.ali.network;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.manager.AliServerRegistry;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import com.yanny.ali.plugin.server.ItemCollectorUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

public abstract class AbstractServer {
    private static final int MAX_CHUNK_SIZE = 32 * 1024; // 32 KB
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#0.00");
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<LootDataChunkMessage> chunks = new ArrayList<>();

    public final void readLootTables(ReloadableServerRegistries.Holder manager, ServerLevel level) {
        LOGGER.info("Started reading loot info");

        long startTime = System.currentTimeMillis();
        AliConfig config = PluginManager.COMMON_REGISTRY.getConfiguration();

        AliServerRegistry serverRegistry = PluginManager.SERVER_REGISTRY;
        Map<ResourceLocation, LootTable> lootTables = collectLootTables(manager);
        Map<ResourceLocation, IDataNode> lootNodes = new HashMap<>();
        Map<ResourceLocation, LootTable> unprocessedLootTables = new HashMap<>(lootTables);
        Map<ResourceLocation, List<Item>> lootTableItems;
        Map<ResourceLocation, List<ItemStack>> lootTableItemStacks;
        List<ILootModifier<?>> lootModifiers = serverRegistry.getLootModifiers();
        Map<ILootModifier.IType<?>, List<ILootModifier<?>>> groupedTypes = lootModifiers.stream().collect(Collectors.groupingBy(ILootModifier::getType));
        List<ILootModifier<?>> blockLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.BLOCK, Collections.emptyList());
        List<ILootModifier<?>> entityLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.ENTITY, Collections.emptyList());
        List<ILootModifier<?>> lootTableLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.LOOT_TABLE, Collections.emptyList());
        Map<ResourceLocation, IDataNode> tradeNodes;
        Map<ResourceLocation, Pair<List<Item>, List<Item>>> tradeItems = new HashMap<>();
        Pair<List<Item>, List<Item>> wanderingTraderItems = ItemCollectorUtils.collectTradeItems(serverRegistry, VillagerTrades.WANDERING_TRADER_TRADES);
        IDataNode wanderingTraderNode = processWanderingTrader(level, serverRegistry);

        serverRegistry.setServerLevel(level);
        lootTables.forEach(serverRegistry::addLootTable); // used for table references
        lootTableItems = lootTables.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, AbstractServer::getItems));

        chunks.clear();

        // apply modifiers
        lootNodes.putAll(processBlocks(serverRegistry, config, unprocessedLootTables, blockLootModifiers, lootTableLootModifiers, lootTableItems));
        lootNodes.putAll(processEntities(serverRegistry, config, level, unprocessedLootTables, entityLootModifiers, lootTableLootModifiers, lootTableItems));
        lootNodes.putAll(processLootTables(serverRegistry, config, unprocessedLootTables, lootTableLootModifiers, lootTableItems));

        lootTableItemStacks = lootNodes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> collectItems(e.getValue())));
        lootNodes = removeEmptyLootTable(serverRegistry, lootNodes, lootTableItemStacks);
        tradeNodes = new HashMap<>(processTrades(level, serverRegistry, config, tradeItems));

        LOGGER.info("Processing {} loot tables and {} trades took {}ms", lootNodes.size(), tradeNodes.size() + 1, System.currentTimeMillis() - startTime);

        // storing and compressing data
        ByteBuf rawBuf = Unpooled.buffer();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(rawBuf, level.registryAccess());

        writeLootData(buf, lootTableItemStacks, lootNodes);
        writeTradeData(buf, tradeNodes, tradeItems, wanderingTraderNode, wanderingTraderItems);
        compressAndStoreData(rawBuf);

        serverRegistry.clearLootTables(); // not needed anymore
        serverRegistry.printRuntimeInfo();
    }

    public final void syncLootTables(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            LOGGER.info("Started syncing loot info to {}", player.getScoreboardName());
            sendStartMessage(serverPlayer, new StartMessage(chunks.size()));

            for (LootDataChunkMessage message : chunks) {
                try {
                    sendLootDataChunkMessage(serverPlayer, message);
                } catch (Throwable e) {
                    LOGGER.warn("Failed to send message with error: {}", e.getMessage(), e);
                }
            }

            sendDoneMessage(serverPlayer, new DoneMessage());
            LOGGER.info("Finished syncing loot info to {}", player.getScoreboardName());
        }
    }

    protected abstract void sendStartMessage(ServerPlayer serverPlayer, StartMessage message);

    protected abstract void sendLootDataChunkMessage(ServerPlayer serverPlayer, LootDataChunkMessage message);

    protected abstract void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message);

    @NotNull
    private static List<Item> getItems(Map.Entry<ResourceLocation, LootTable> lootTableMap) {
        return ItemCollectorUtils.collectLootTable(PluginManager.SERVER_REGISTRY, lootTableMap.getValue());
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> removeEmptyLootTable(AliServerRegistry serverRegistry, Map<ResourceLocation, IDataNode> lootNodes, Map<ResourceLocation, List<ItemStack>> items) {
        Map<ResourceLocation, IDataNode> result = new HashMap<>();
        int emptyLootTables = 0;
        int injectedLootTables = 0;

        for (Map.Entry<ResourceLocation, IDataNode> entry : lootNodes.entrySet()) {
            IDataNode node = entry.getValue();

            if (node instanceof ListNode listNode) {
                listNode.optimizeList();
            }

            if (!items.getOrDefault(entry.getKey(), Collections.emptyList()).isEmpty()) {
                if (!serverRegistry.isSubTable(entry.getKey())) {
                    result.put(entry.getKey(), node);
                } else {
                    injectedLootTables++;
                }
            } else {
                emptyLootTables++;
            }
        }

        LOGGER.info("Skipped {} empty or hidden loot tables and {} injected loot tables", emptyLootTables, injectedLootTables);
        return result;
    }

    @NotNull
    private static Map<ResourceLocation, LootTable> collectLootTables(ReloadableServerRegistries.Holder manager) {
        Map<ResourceLocation, LootTable> lootTables = new HashMap<>();

        manager.get().lookup(Registries.LOOT_TABLE).ifPresent(
                (lookup) -> lookup.listElements().forEach((reference) -> lootTables.put(reference.key().location(), reference.value()))
        );
        return lootTables;
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> processBlocks(AliServerRegistry serverRegistry, AliConfig config, Map<ResourceLocation, LootTable> lootTables,
                                                                  List<ILootModifier<?>> blockLootModifiers, List<ILootModifier<?>> lootTableLootModifiers,
                                                                  Map<ResourceLocation, List<Item>> lootTableItems) {
        Map<ResourceLocation, IDataNode> lootNodes = new HashMap<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceKey<LootTable> resourceKey = block.getLootTable();

            //noinspection ConstantValue
            if (resourceKey != null) {
                ResourceLocation location = resourceKey.location();
                LootTable lootTable = lootTables.remove(location);

                serverRegistry.setCurrentLootTable(location);

                if (config.blockCategories.stream().filter((f) -> f.validate(block)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                    List<Item> items = lootTableItems.getOrDefault(location, Collections.emptyList());
                    List<ILootModifier<?>> lootModifiers = Stream.concat(
                            blockLootModifiers.stream().filter((m) -> predicateModifier(m, block, items)),
                            lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items))
                    ).toList();

                    try {
                        if (lootTable != null) {
                            lootNodes.put(location, serverRegistry.parseTable(lootModifiers, lootTable));
                        } else if (!lootModifiers.isEmpty()) {
                            lootNodes.put(location, serverRegistry.parseTable(lootModifiers));
                        } else {
                            LOGGER.debug("Missing block loot table for {}", block);
                        }
                    } catch (Throwable e) {
                        LOGGER.warn("Failed to parse block loot table {} with error {}", location, e.getMessage(), e);
                    }
                }

                serverRegistry.setCurrentLootTable(null);
            }
        }

        return lootNodes;
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> processEntities(AliServerRegistry serverRegistry, AliConfig config, ServerLevel level, Map<ResourceLocation, LootTable> lootTables,
                                                                    List<ILootModifier<?>> entityLootModifiers, List<ILootModifier<?>> lootTableLootModifiers,
                                                                    Map<ResourceLocation, List<Item>> lootTableItems) {
        Map<ResourceLocation, IDataNode> lootNodes = new HashMap<>();

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (config.disabledEntities.stream().anyMatch((f) -> f.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)))) {
                lootTables.remove(entityType.getDefaultLootTable().location()); // at least remove entity default loot table, otherwise it will end up in gameplay category
                continue;
            }

            List<Entity> entityList = serverRegistry.createEntities(entityType, level);

            for (Entity entity : entityList) {
                if (entity instanceof Mob mob) {
                    ResourceKey<LootTable> resourceKey = mob.getLootTable();

                    //noinspection ConstantValue
                    if (resourceKey != null) {
                        ResourceLocation location = resourceKey.location();
                        LootTable lootTable = lootTables.remove(location);

                        serverRegistry.setCurrentLootTable(location);

                        if (config.entityCategories.stream().filter((f) -> f.validate(entityType)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                            List<Item> items = lootTableItems.getOrDefault(location, Collections.emptyList());
                            List<ILootModifier<?>> lootModifiers = Stream.concat(
                                    entityLootModifiers.stream().filter((m) -> predicateModifier(m, entity, items)),
                                    lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items))
                            ).toList();

                            try {
                                if (lootTable != null) {
                                    lootNodes.put(location, serverRegistry.parseTable(lootModifiers, lootTable));
                                } else if (!lootModifiers.isEmpty()) {
                                    lootNodes.put(location, serverRegistry.parseTable(lootModifiers));
                                } else {
                                    LOGGER.debug("Missing entity loot table for {}", entity);
                                }
                            } catch (Throwable e) {
                                LOGGER.warn("Failed to parse entity loot table {} with error {}", location, e.getMessage(), e);
                            }
                        }

                        serverRegistry.setCurrentLootTable(null);
                    }
                }
            }
        }

        return lootNodes;
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> processLootTables(AliServerRegistry serverRegistry, AliConfig config, Map<ResourceLocation, LootTable> lootTables,
                                                                      List<ILootModifier<?>> lootTableLootModifiers, Map<ResourceLocation, List<Item>> lootTableItems) {
        Map<ResourceLocation, IDataNode> lootNodes = new HashMap<>();

        for (Map.Entry<ResourceLocation, LootTable> entry : lootTables.entrySet()) {
            ResourceLocation location = entry.getKey();

            serverRegistry.setCurrentLootTable(location);

            if (config.gameplayCategories.stream().filter((f) -> f.validate(location)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                LootTable lootTable = entry.getValue();
                List<Item> items = lootTableItems.get(location);
                List<ILootModifier<?>> lootModifiers = lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items)).toList();

                try {
                    lootNodes.put(location, serverRegistry.parseTable(lootModifiers, lootTable));
                } catch (Throwable e) {
                    LOGGER.warn("Failed to parse loot table {} with error {}", location, e.getMessage(), e);
                }
            }

            serverRegistry.setCurrentLootTable(null);
        }

        lootTables.clear();
        return lootNodes;
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> processTrades(ServerLevel level, AliServerRegistry serverRegistry, AliConfig config, Map<ResourceLocation, Pair<List<Item>, List<Item>>> tradeItems) {
        Map<ResourceLocation, IDataNode> nodes = new HashMap<>();
        Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> trades = VillagerTrades.TRADES;
        Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> experimentalTrades = VillagerTrades.EXPERIMENTAL_TRADES;

        for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : BuiltInRegistries.VILLAGER_PROFESSION.entrySet()) {
            ResourceLocation location = entry.getKey().location();

            serverRegistry.setCurrentLootTable(location);

            if (config.tradeCategories.stream().filter((f) -> f.validate(location)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                Int2ObjectMap<VillagerTrades.ItemListing[]> itemListingMap = null;

                if (level.enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
                    itemListingMap = experimentalTrades.get(entry.getValue());
                }

                if (itemListingMap == null) {
                    itemListingMap = trades.get(entry.getValue());
                }

                if (itemListingMap != null && itemListingMap.int2ObjectEntrySet().stream().anyMatch((e) -> e.getValue().length > 0)) {
                    try {
                        nodes.put(location, serverRegistry.parseTrade(itemListingMap));
                        tradeItems.put(location, ItemCollectorUtils.collectTradeItems(serverRegistry, itemListingMap));
                    } catch (Throwable e) {
                        LOGGER.warn("Failed to parse trade for villager {} with error {}", location, e.getMessage(), e);
                    }
                } else {
                    LOGGER.warn("No trades defined for {}", location);
                }
            }

            serverRegistry.setCurrentLootTable(null);
        }

        return nodes;
    }

    @NotNull
    private static IDataNode processWanderingTrader(ServerLevel level, AliServerRegistry serverRegistry) {
        try {
            if (level.enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
                Int2ObjectMap<VillagerTrades.ItemListing[]> trades = new Int2ObjectOpenHashMap<>();

                VillagerTrades.EXPERIMENTAL_WANDERING_TRADER_TRADES.forEach((pair) -> trades.put((int) pair.getValue(), pair.getKey()));
                return serverRegistry.parseTrade(trades);
            } else {
                return serverRegistry.parseTrade(VillagerTrades.WANDERING_TRADER_TRADES);
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to parse wandering trader with error {}", e.getMessage(), e);
            return new MissingNode(EmptyTooltipNode.EMPTY);
        }
    }

    private static <T> boolean predicateModifier(ILootModifier<?> modifier, T value, List<Item> items) {
        //noinspection unchecked
        return ((ILootModifier<T>) modifier).predicate(value) && predicateItem(modifier, items);
    }

    private static boolean predicateItem(ILootModifier<?> modifier, List<Item> items) { //FIXME ItemStack!
        if (!items.isEmpty()) {
            return items.stream().anyMatch((i) -> modifier.getOperations().stream().anyMatch(o -> o.predicate().test(i.getDefaultInstance())));
        } else {
            return true;
        }
    }

    @NotNull
    private static List<ItemStack> collectItems(IDataNode node) {
        List<ItemStack> itemStacks = new ArrayList<>();

        if (node instanceof ListNode listNode) {
            for (IDataNode n : listNode.nodes()) {
                itemStacks.addAll(collectItems(n));
            }
        } else if (node instanceof IItemNode itemNode) {
            itemStacks.addAll(itemNode.getModifiedItem().map(List::of, AbstractServer::toItemStacks));
        }

        return itemStacks;
    }

    private void writeLootData(RegistryFriendlyByteBuf buf, Map<ResourceLocation, List<ItemStack>> lootTableItemStacks, Map<ResourceLocation, IDataNode> lootNodes) {
        AliServerRegistry utils = PluginManager.SERVER_REGISTRY;
        int countIndex = buf.writerIndex();
        int successfulNodes = 0;

        buf.writeInt(lootNodes.size());

        for (Map.Entry<ResourceLocation, IDataNode> nodeEntry : lootNodes.entrySet()) {
            int startOfNode = buf.writerIndex();

            try {
                utils.setCurrentLootTable(nodeEntry.getKey());
                buf.writeResourceLocation(nodeEntry.getKey());
                nodeEntry.getValue().encode(utils, buf);
                ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(buf, lootTableItemStacks.getOrDefault(nodeEntry.getKey(), Collections.emptyList()));
                successfulNodes++;
            } catch (Throwable e) {
                buf.writerIndex(startOfNode);
                LOGGER.warn("Failed to write loot data in {}", nodeEntry.getKey(), e);
            } finally {
                utils.setCurrentLootTable(null);
            }
        }

        if (successfulNodes != lootNodes.size()) {
            int endIndex = buf.writerIndex();

            buf.writerIndex(countIndex);
            buf.writeInt(successfulNodes);
            buf.writerIndex(endIndex);
        }

        lootNodes.clear();
        lootTableItemStacks.clear();
    }

    private void writeTradeData(RegistryFriendlyByteBuf buf, Map<ResourceLocation, IDataNode> trades, Map<ResourceLocation, Pair<List<Item>, List<Item>>> items, IDataNode wanderingTraderNode, Pair<List<Item>, List<Item>> wanderingTraderItems) {
        AliServerRegistry utils = PluginManager.SERVER_REGISTRY;
        int countIndex = buf.writerIndex();
        int successfulNodes = 0;

        buf.writeInt(trades.size());

        for (Map.Entry<ResourceLocation, IDataNode> nodeEntry : trades.entrySet()) {
            int startOfNode = buf.writerIndex();

            try {
                Pair<List<Item>, List<Item>> pair = items.getOrDefault(nodeEntry.getKey(), new Pair<>(Collections.emptyList(), Collections.emptyList()));

                utils.setCurrentLootTable(nodeEntry.getKey());
                buf.writeResourceLocation(nodeEntry.getKey());
                nodeEntry.getValue().encode(utils, buf);
                buf.writeCollection(pair.getA(), (b, i) -> b.writeResourceLocation(BuiltInRegistries.ITEM.getKey(i)));
                buf.writeCollection(pair.getB(), (b, i) -> b.writeResourceLocation(BuiltInRegistries.ITEM.getKey(i)));
                successfulNodes++;
            } catch (Throwable e) {
                buf.writerIndex(startOfNode);
                LOGGER.warn("Failed to write trade data in {}", nodeEntry.getKey(), e);
            } finally {
                utils.setCurrentLootTable(null);
            }
        }

        if (successfulNodes != trades.size()) {
            int endIndex = buf.writerIndex();

            buf.writerIndex(countIndex);
            buf.writeInt(successfulNodes);
            buf.writerIndex(endIndex);
        }

        int wtStart = buf.writerIndex();

        try {
            utils.setCurrentLootTable(ResourceLocation.withDefaultNamespace("wandering_trader"));
            wanderingTraderNode.encode(utils, buf);
            buf.writeCollection(wanderingTraderItems.getA(), (b, i) -> b.writeResourceLocation(BuiltInRegistries.ITEM.getKey(i)));
            buf.writeCollection(wanderingTraderItems.getB(), (b, i) -> b.writeResourceLocation(BuiltInRegistries.ITEM.getKey(i)));
        } catch (Throwable e) {
            LOGGER.warn("Failed to encode Wandering Trader", e);

            // write dummy data
            buf.writerIndex(wtStart);
            new TradeNode(utils, new Int2ObjectOpenHashMap<>()).encode(utils, buf);
            buf.writeCollection(List.of(), (b, i) -> {});
            buf.writeCollection(List.of(), (b, i) -> {});
        } finally {
            utils.setCurrentLootTable(null);
        }

        trades.clear();
        items.clear();
    }

    private void compressAndStoreData(ByteBuf rawBuf) {
        int rawSize = rawBuf.readableBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(rawSize);

        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            rawBuf.readBytes(gzip, rawBuf.readableBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] compressedData = bos.toByteArray();
        int totalChunks = (int) Math.ceil((double) compressedData.length / MAX_CHUNK_SIZE);

        for (int i = 0; i < totalChunks; i++) {
            int offset = i * MAX_CHUNK_SIZE;
            int length = Math.min(MAX_CHUNK_SIZE, compressedData.length - offset);
            byte[] chunkData = new byte[length];

            System.arraycopy(compressedData, offset, chunkData, 0, length);
            chunks.add(new LootDataChunkMessage(i, chunkData));
        }

        rawBuf.release();

        LOGGER.info("Compressed loot data ({} MB -> {} MB) and stored in {} chunk(s)",
                DOUBLE_FORMAT.format(rawSize / 1024.0 / 1024.0),
                DOUBLE_FORMAT.format(compressedData.length / 1024.0 / 1024.0),
                totalChunks);
    }

    private static <T extends ItemLike> List<ItemStack> toItemStacks(TagKey<T> tag) {
        //noinspection unchecked
        Registry<T> registry = (Registry<T>)BuiltInRegistries.REGISTRY.get(tag.registry().location());

        if (registry != null) {
            return registry
                    .getTag(tag)
                    .map(holders -> holders.stream().map(Holder::value).map((i) -> i.asItem().getDefaultInstance()).toList())
                    .orElse(Collections.emptyList());
        } else {
            return Collections.emptyList();
        }
    }
}
