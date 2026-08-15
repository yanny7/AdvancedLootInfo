package com.yanny.ali.network;

import com.mojang.logging.LogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.network.NetworkUtils;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.manager.AliServerRegistry;
import com.yanny.ali.manager.FakeLootDataManager;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.common.EntityLootTableResolver;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.ItemCollectorUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractServer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<LootDataChunkMessage> chunks = new ArrayList<>();
    private FakeLootDataManager fakeLootDataManager;

    public FakeLootDataManager getFakeLootDataManager(HolderLookup.Provider provider) {
        if (fakeLootDataManager == null) {
            fakeLootDataManager = new FakeLootDataManager(provider);
        }

        return fakeLootDataManager;
    }

    public final void readLootTables(ReloadableServerRegistries.Holder manager) {
        LOGGER.info("Started reading loot info");

        long startTime = System.currentTimeMillis();
        AliConfig config = PluginManager.getInstance().commonRegistry.getConfiguration();

        AliServerRegistry serverRegistry = PluginManager.getInstance().serverRegistry;
        Map<Identifier, LootTable> lootTables = collectLootTables(manager);
        Map<Identifier, IDataNode> lootNodes = new HashMap<>();
        Map<Identifier, LootTable> unprocessedLootTables = new HashMap<>(lootTables);
        Map<Identifier, LootTable> fakeLootTables = new HashMap<>(fakeLootDataManager.getLootTables());
        Map<Identifier, List<Item>> lootTableItems;
        List<ILootModifier<?>> lootModifiers = serverRegistry.getLootModifiers();
        Map<ILootModifier.IType<?>, List<ILootModifier<?>>> groupedTypes = lootModifiers.stream().collect(Collectors.groupingBy(ILootModifier::getType));
        List<ILootModifier<?>> blockLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.BLOCK, Collections.emptyList());
        List<ILootModifier<?>> entityLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.ENTITY, Collections.emptyList());
        List<ILootModifier<?>> lootTableLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.LOOT_TABLE, Collections.emptyList());
        Map<Identifier, IDataNode> tradeNodes;
        IDataNode wanderingTraderNode = processWanderingTrader(serverRegistry, manager);

        lootTables.forEach(serverRegistry::addLootTable); // used for table references
        lootTableItems = collectLootTableItems(lootTables, fakeLootTables);

        chunks.clear();

        // apply modifiers
        lootNodes.putAll(processBlocks(serverRegistry, config, unprocessedLootTables, fakeLootTables, blockLootModifiers, lootTableLootModifiers, lootTableItems));
        lootNodes.putAll(processEntities(serverRegistry, config, serverRegistry.getServerLevel(), unprocessedLootTables, fakeLootTables, entityLootModifiers, lootTableLootModifiers, lootTableItems));
        lootNodes.putAll(processLootTables(serverRegistry, config, unprocessedLootTables, fakeLootTables, lootTableLootModifiers, lootTableItems));

        lootNodes = removeEmptyLootTable(serverRegistry, lootNodes);
        tradeNodes = new HashMap<>(processTrades(serverRegistry, config));

        LOGGER.info("Processing {} loot tables, {} fake loot tables and {} trades took {}ms", lootNodes.size(), fakeLootTables.size(), tradeNodes.size() + 1, System.currentTimeMillis() - startTime);

        ByteBuf rawBuf = Unpooled.buffer();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(rawBuf, serverRegistry.getServerLevel().registryAccess());

        // storing and compressing data
        serverRegistry.getTooltipCache().encode(serverRegistry, buf);
        NetworkUtils.writeMapData(Utils.MOD_ID, serverRegistry, buf, lootNodes);
        NetworkUtils.writeMapData(Utils.MOD_ID, serverRegistry, buf, tradeNodes);

        if (!NetworkUtils.writeNodeData(Utils.MOD_ID, serverRegistry, buf, Identifier.withDefaultNamespace("wandering_trader"), wanderingTraderNode)) {
            new TradeNode(serverRegistry, new Int2ObjectOpenHashMap<>(), true).encode(serverRegistry, buf);
        }

        NetworkUtils.compressAndStoreData(Utils.MOD_ID, rawBuf, (i, data) -> chunks.add(new LootDataChunkMessage(i, data)));

        serverRegistry.printRuntimeInfo();

        fakeLootDataManager.clearLootTables();
        serverRegistry.clearTemporaryMaps(); // not needed anymore
        serverRegistry.getTooltipCache().clear();
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
    private static List<Item> getItems(Map.Entry<Identifier, LootTable> lootTableMap) {
        return ItemCollectorUtils.collectLootTable(PluginManager.getInstance().serverRegistry, lootTableMap.getValue());
    }

    /**
     * The items a table produces are collected here only to tell an empty table from a real one - the client derives
     * its own from the very same node tree ({@code GenericUtils.collectItems}), so nothing item-shaped is sent.
     */
    @NotNull
    private static Map<Identifier, IDataNode> removeEmptyLootTable(AliServerRegistry serverRegistry, Map<Identifier, IDataNode> lootNodes) {
        Map<Identifier, IDataNode> result = new HashMap<>();
        int emptyLootTables = 0;
        int injectedLootTables = 0;

        for (Map.Entry<Identifier, IDataNode> entry : lootNodes.entrySet()) {
            IDataNode node = entry.getValue();

            if (node instanceof ListNode listNode) {
                listNode.optimizeList();
            }

            if (!collectItems(node).isEmpty()) {
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
    private static Map<Identifier, LootTable> collectLootTables(ReloadableServerRegistries.Holder manager) {
        Map<Identifier, LootTable> lootTables = new HashMap<>();
        Registry<LootTable> registry = (Registry<LootTable>)manager.lookup().lookup(Registries.LOOT_TABLE).orElseThrow();

        registry.entrySet().forEach((e) -> lootTables.put(e.getKey().identifier(), e.getValue()));

        return lootTables;
    }

    @NotNull
    private static Map<Identifier, IDataNode> processBlocks(AliServerRegistry serverRegistry, AliConfig config, Map<Identifier, LootTable> lootTables,
                                                            Map<Identifier, LootTable> fakeLootTables, List<ILootModifier<?>> blockLootModifiers,
                                                            List<ILootModifier<?>> lootTableLootModifiers, Map<Identifier, List<Item>> lootTableItems) {
        Map<Identifier, IDataNode> lootNodes = new HashMap<>();
        Map<Identifier, List<Block>> blocksByLootTable = new LinkedHashMap<>();
        int defaultDropLootTables = 0;

        for (Block block : BuiltInRegistries.BLOCK) {
            blocksByLootTable.computeIfAbsent(Utils.getLootTableKey(block), (k) -> new ArrayList<>()).add(block);
        }

        // blocks sharing a single loot table (vanilla's dropsLike, e.g. wall banner -> banner) all contribute their
        // loot modifiers to the one node stored under that table's id, instead of the first block claiming it
        for (Map.Entry<Identifier, List<Block>> entry : blocksByLootTable.entrySet()) {
            Identifier location = entry.getKey();
            List<Block> blocks = entry.getValue().stream()
                    .filter((b) -> config.blockCategories.stream().filter((f) -> f.validate(b)).findFirst().map((f) -> !f.isHidden()).orElse(false))
                    .toList();
            LootTable lootTable = lootTables.remove(location);

            TooltipContext.set(location);

            if (!blocks.isEmpty()) {
                List<Item> items = lootTableItems.getOrDefault(location, Collections.emptyList());
                List<ILootModifier<?>> lootModifiers = Stream.concat(
                        blockLootModifiers.stream().filter((m) -> blocks.stream().anyMatch((b) -> predicateModifier(m, b, items))),
                        lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items))
                ).toList();

                if (config.hideDefaultBlockLoot && lootModifiers.isEmpty() && !fakeLootTables.containsKey(location)
                        && blocks.stream().anyMatch((b) -> isDefaultBlockDrop(serverRegistry, config, b, lootTable))) {
                    defaultDropLootTables++;
                    TooltipContext.clear();
                    continue;
                }

                try {
                    if (lootTable != null) {
                        IDataNode node = serverRegistry.parseTable(lootModifiers, lootTable);
                        List<IDataNode> fakePools = getFakeLootPools(location, serverRegistry, fakeLootTables);

                        if (node instanceof LootTableNode lootTableNode) {
                            fakePools.forEach(lootTableNode::addChildren);
                        }

                        lootNodes.put(location, node);
                    } else if (!lootModifiers.isEmpty()) {
                        IDataNode node = serverRegistry.parseTable(lootModifiers);
                        List<IDataNode> fakePools = getFakeLootPools(location, serverRegistry, fakeLootTables);

                        if (node instanceof LootTableNode lootTableNode) {
                            fakePools.forEach(lootTableNode::addChildren);
                        }

                        lootNodes.put(location, node);
                    } else {
                        LootTable fakeLootTable = fakeLootTables.get(location);

                        if (fakeLootTable != null) {
                            lootNodes.put(location, serverRegistry.parseTable(Collections.emptyList(), fakeLootTable));
                        } else if (blocks.stream().noneMatch((b) -> b.getLootTable().isEmpty())) {
                            // noLootTable() blocks are keyed by themselves and have no table by definition
                            LOGGER.debug("Missing block loot table for {}", location);
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.warn("Failed to parse block loot table {} with error {}", location, e.getMessage(), e);
                }
            }

            TooltipContext.clear();
        }

        if (defaultDropLootTables > 0) {
            LOGGER.info("Skipped {} block loot tables dropping only the block itself", defaultDropLootTables);
        }

        return lootNodes;
    }

    /**
     * Matches the shape produced by vanilla's {@code dropSelf}: a single pool with exactly one roll, holding a single
     * {@link LootItem} for the block's own item. Functions and conditions make the table non-default unless their type
     * is listed in the config's {@code defaultBlockLootFunctions}/{@code defaultBlockLootConditions}.
     */
    private static boolean isDefaultBlockDrop(AliServerRegistry serverRegistry, AliConfig config, Block block, @Nullable LootTable lootTable) {
        if (lootTable == null || !isIgnoredFunctions(config, lootTable.functions)) {
            return false;
        }

        List<LootPool> pools = lootTable.pools;

        if (pools.size() != 1) {
            return false;
        }

        LootPool pool = pools.getFirst();

        if (pool.entries.size() != 1 || !isIgnoredFunctions(config, pool.functions) || !isIgnoredConditions(config, pool.conditions)
                || !isConstant(serverRegistry, pool.rolls, 1) || !isConstant(serverRegistry, pool.bonusRolls, 0)) {
            return false;
        }

        if (!(pool.entries.getFirst() instanceof LootItem lootItem) || lootItem.item.value() != block.asItem() || !isIgnoredFunctions(config, lootItem.functions)) {
            return false;
        }

        return isIgnoredConditions(config, lootItem.conditions);
    }

    private static boolean isIgnoredFunctions(AliConfig config, List<LootItemFunction> functions) {
        return functions.stream().allMatch((f) -> config.defaultBlockLootFunctions.contains(BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(f.codec())));
    }

    private static boolean isIgnoredConditions(AliConfig config, List<LootItemCondition> conditions) {
        return conditions.stream().allMatch((c) -> config.defaultBlockLootConditions.contains(BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(c.codec())));
    }

    private static boolean isConstant(AliServerRegistry serverRegistry, NumberProvider numberProvider, float value) {
        RangeValue range = serverRegistry.convertNumber(serverRegistry, numberProvider);

        return !range.isUnknown() && range.min() == value && range.max() == value;
    }

    @NotNull
    private static Map<Identifier, IDataNode> processEntities(AliServerRegistry serverRegistry, AliConfig config, ServerLevel level, Map<Identifier, LootTable> lootTables,
                                                              Map<Identifier, LootTable> fakeLootTables, List<ILootModifier<?>> entityLootModifiers,
                                                              List<ILootModifier<?>> lootTableLootModifiers, Map<Identifier, List<Item>> lootTableItems) {
        Map<Identifier, IDataNode> lootNodes = new HashMap<>();
        EntityLootTableResolver resolver = new EntityLootTableResolver(serverRegistry, level);
        Set<Identifier> candidates = new HashSet<>(lootTables.keySet());
        Set<Identifier> disabledLootTables = new HashSet<>();

        candidates.addAll(fakeLootTables.keySet());

        if (!entityLootModifiers.isEmpty()) {
            // a modifier can add drops to an entity whose own loot table does not exist, so those tables have to be
            // considered even though no data references them
            BuiltInRegistries.ENTITY_TYPE.forEach((entityType) -> entityType.getDefaultLootTable().ifPresent((resourceKey) -> candidates.add(resourceKey.identifier())));
        }

        // same grouping as for blocks: all entities sharing a loot table contribute to the single node under its id
        for (Map.Entry<Identifier, List<EntityType<?>>> entry : resolver.resolveAll(candidates).entrySet()) {
            Identifier location = entry.getKey();

            if (entry.getValue().stream().allMatch((t) -> config.disabledEntities.stream().anyMatch((f) -> f.equals(BuiltInRegistries.ENTITY_TYPE.getKey(t))))) {
                disabledLootTables.add(location); // remove the table, otherwise it will end up in gameplay category
                continue;
            }

            List<EntityType<?>> entityTypes = entry.getValue().stream()
                    .filter((t) -> config.entityCategories.stream().filter((f) -> f.validate(t)).findFirst().map((f) -> !f.isHidden()).orElse(false))
                    .toList();
            LootTable lootTable = lootTables.remove(location);

            TooltipContext.set(location);

            if (!entityTypes.isEmpty()) {
                List<Item> items = lootTableItems.getOrDefault(location, Collections.emptyList());
                List<ILootModifier<?>> lootModifiers = Stream.concat(
                        // the only step that still needs an instance, and only when global loot modifiers exist at all
                        entityLootModifiers.stream().filter((m) -> entityTypes.stream().anyMatch((t) -> resolver.getEntities(t).stream().anyMatch((e) -> predicateModifier(m, e, items)))),
                        lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items))
                ).toList();

                try {
                    if (lootTable != null) {
                        IDataNode node = serverRegistry.parseTable(lootModifiers, lootTable);
                        List<IDataNode> fakePools = getFakeLootPools(location, serverRegistry, fakeLootTables);

                        if (node instanceof LootTableNode lootTableNode) {
                            fakePools.forEach(lootTableNode::addChildren);
                        }

                        lootNodes.put(location, node);
                    } else if (!lootModifiers.isEmpty()) {
                        IDataNode node = serverRegistry.parseTable(lootModifiers);
                        List<IDataNode> fakePools = getFakeLootPools(location, serverRegistry, fakeLootTables);

                        if (node instanceof LootTableNode lootTableNode) {
                            fakePools.forEach(lootTableNode::addChildren);
                        }

                        lootNodes.put(location, node);
                    } else {
                        LootTable fakeLootTable = fakeLootTables.get(location);

                        if (fakeLootTable != null) {
                            lootNodes.put(location, serverRegistry.parseTable(Collections.emptyList(), fakeLootTable));
                        } else {
                            LOGGER.debug("Missing entity loot table for {}", location);
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.warn("Failed to parse entity loot table {} with error {}", location, e.getMessage(), e);
                }
            }

            TooltipContext.clear();
        }

        lootTables.keySet().removeAll(disabledLootTables);
        return lootNodes;
    }

    @NotNull
    private static Map<Identifier, IDataNode> processLootTables(AliServerRegistry serverRegistry, AliConfig config, Map<Identifier, LootTable> lootTables,
                                                                      Map<Identifier, LootTable> fakeLootTables, List<ILootModifier<?>> lootTableLootModifiers,
                                                                      Map<Identifier, List<Item>> lootTableItems) {
        Map<Identifier, IDataNode> lootNodes = new HashMap<>();

        for (Map.Entry<Identifier, LootTable> entry : lootTables.entrySet()) {
            Identifier location = entry.getKey();

            TooltipContext.set(location);

            if (config.gameplayCategories.stream().filter((f) -> f.validate(location)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                LootTable lootTable = entry.getValue();
                List<Item> items = lootTableItems.get(location);
                List<ILootModifier<?>> lootModifiers = lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items)).toList();

                try {
                    IDataNode node = serverRegistry.parseTable(lootModifiers, lootTable);
                    List<IDataNode> fakePools = getFakeLootPools(location, serverRegistry, fakeLootTables);

                    if (node instanceof LootTableNode lootTableNode) {
                        fakePools.forEach(lootTableNode::addChildren);
                    }

                    lootNodes.put(location, node);
                } catch (Throwable e) {
                    LOGGER.warn("Failed to parse loot table {} with error {}", location, e.getMessage(), e);
                }
            }

            TooltipContext.clear();
        }

        lootTables.clear();
        return lootNodes;
    }

    @NotNull
    private static Map<Identifier, IDataNode> processTrades(AliServerRegistry serverRegistry, AliConfig config) {
        Map<Identifier, IDataNode> nodes = new HashMap<>();
        HolderLookup.RegistryLookup<TradeSet> lookup = serverRegistry.lookupProvider().lookup(Registries.TRADE_SET).orElseThrow();

        for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : BuiltInRegistries.VILLAGER_PROFESSION.entrySet()) {
            Identifier location = entry.getKey().identifier();
            VillagerProfession profession = entry.getValue();
            List<Int2ObjectMap.Entry<ResourceKey<TradeSet>>> entries = profession.tradeSetsByLevel().int2ObjectEntrySet()
                    .stream()
                    .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
                    .toList();
            Pair<List<Item>, List<Item>> items = new Pair<>(new ArrayList<>(), new ArrayList<>());

            for (Int2ObjectMap.Entry<ResourceKey<TradeSet>> entryTrades : entries) {
                Optional<Holder.Reference<TradeSet>> tradeSetReference = lookup.get(entryTrades.getValue());

                tradeSetReference.ifPresent((tradeSet) -> {
                    for (Holder<VillagerTrade> trade : tradeSet.value().getTrades()) {
                        Pair<List<Item>, List<Item>> pair = TradeUtils.collectItems(serverRegistry, trade.value());
                        items.getA().addAll(pair.getA());
                        items.getB().addAll(pair.getB());
                    }
                });
            }

            if (items.getA().isEmpty() && items.getB().isEmpty()) {
                LOGGER.warn("No trades defined for profession {}", location);
                continue;
            }

            tradeItems.put(location, items);
            TooltipContext.set(location);

            if (config.tradeCategories.stream().filter((f) -> f.validate(location)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                try {
                    nodes.put(location, serverRegistry.parseTrade(profession, false));
                } catch (Throwable e) {
                    LOGGER.warn("Failed to parse trade for villager {} with error {}", location, e.getMessage(), e);
                }
            }

            TooltipContext.clear();
        }

        return nodes;
    }

    @NotNull
    private static IDataNode processWanderingTrader(AliServerRegistry serverRegistry, ReloadableServerRegistries.Holder manager) {
        try {
            List<TradeSet> trades = new ArrayList<>();
            Registry<TradeSet> registry = (Registry<TradeSet>)manager.lookup().lookup(Registries.TRADE_SET).orElseThrow();

            registry.get(TradeSets.WANDERING_TRADER_BUYING).ifPresent((tradeSet) -> trades.add(tradeSet.value()));
            registry.get(TradeSets.WANDERING_TRADER_COMMON).ifPresent((tradeSet) -> trades.add(tradeSet.value()));
            registry.get(TradeSets.WANDERING_TRADER_UNCOMMON).ifPresent((tradeSet) -> trades.add(tradeSet.value()));

            return serverRegistry.parseTrade(trades, true);
        } catch (Throwable e) {
            LOGGER.warn("Failed to parse wandering trader with error {}", e.getMessage(), e);
            return new MissingNode(TooltipNode.empty());
        }
    }

    @NotNull
    private static Pair<List<Item>, List<Item>> collectWanderingTraderItems(AliServerRegistry serverRegistry, ReloadableServerRegistries.Holder manager) {
        List<Item> inputs = new ArrayList<>();
        List<Item> outputs = new ArrayList<>();
        Consumer<Holder.Reference<TradeSet>> collectConsumer = (set) -> {
            Pair<List<Item>, List<Item>> pair = ItemCollectorUtils.collectTradeSetItems(serverRegistry, set.value());

            inputs.addAll(pair.getA());
            outputs.addAll(pair.getB());
        };

        try {
            Registry<TradeSet> registry = (Registry<TradeSet>)manager.lookup().lookup(Registries.TRADE_SET).orElseThrow();

            registry.get(TradeSets.WANDERING_TRADER_BUYING).ifPresent(collectConsumer);
            registry.get(TradeSets.WANDERING_TRADER_COMMON).ifPresent(collectConsumer);
            registry.get(TradeSets.WANDERING_TRADER_UNCOMMON).ifPresent(collectConsumer);
        } catch (Throwable e) {
            LOGGER.warn("Failed to parse wandering trader items with error {}", e.getMessage(), e);
        }

        return new Pair<>(inputs, outputs);
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

    @Unmodifiable
    @NotNull
    private static List<ItemStack> collectItems(IDataNode node) {
        List<ItemStack> itemStacks = new ArrayList<>();

        if (node instanceof ListNode listNode) {
            for (IDataNode n : listNode.nodes()) {
                itemStacks.addAll(collectItems(n));
            }
        } else if (node instanceof IItemNode itemNode) {
            itemStacks.addAll(itemNode.getItems());
        }

        return itemStacks.stream().filter((f) -> !f.isEmpty()).toList();
    }

    private static Map<Identifier, List<Item>> collectLootTableItems(Map<Identifier, LootTable> lootTables, Map<Identifier, LootTable> fakeLootTables) {
        Map<Identifier, List<Item>> items = lootTables.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, AbstractServer::getItems));

        for (Map.Entry<Identifier, LootTable> entry : lootTables.entrySet()) {
            items.put(entry.getKey(), getItems(entry));
        }

        for (Map.Entry<Identifier, LootTable> entry : fakeLootTables.entrySet()) {
            items.compute(entry.getKey(), (key, value) -> {
                List<Item> tableItems = getItems(entry);

                if (value == null) {
                    return tableItems;
                } else {
                    value.addAll(tableItems);
                    return value;
                }
            });
        }

        return items;
    }

    private static List<IDataNode> getFakeLootPools(Identifier location, AliServerRegistry serverRegistry, Map<Identifier, LootTable> fakeLootTables) {
        LootTable fakeLootTable = fakeLootTables.get(location);

        if (fakeLootTable != null) {
            IDataNode fakeNode = serverRegistry.parseTable(Collections.emptyList(), fakeLootTable);

            if (fakeNode instanceof LootTableNode fakeLootTableNode) {
                return fakeLootTableNode.nodes();
            }
        }

        return Collections.emptyList();
    }
}
