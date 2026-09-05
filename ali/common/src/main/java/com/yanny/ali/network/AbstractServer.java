package com.yanny.ali.network;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.network.NetworkUtils;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.manager.AliServerRegistry;
import com.yanny.ali.manager.FakeLootDataManager;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.common.EntityLootTableResolver;
import com.yanny.ali.plugin.common.nodes.EntityLootTableNode;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractServer {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    private final List<LootDataChunkMessage> chunks = new ArrayList<>();
    private final FakeLootDataManager fakeLootDataManager = new FakeLootDataManager();

    public FakeLootDataManager getFakeLootDataManager() {
        return fakeLootDataManager;
    }

    public final void readLootTables(LootDataManager manager) {
        AliServerRegistry serverRegistry = PluginManager.getInstance().serverRegistry;

        TooltipContext.setPalette(serverRegistry.getTooltipCache());

        try {
            readLootTables(manager, serverRegistry);
        } finally {
            TooltipContext.clearPalette();
        }
    }

    private void readLootTables(LootDataManager manager, AliServerRegistry serverRegistry) {
        LOGGER.info("Started reading loot info");

        long startTime = System.currentTimeMillis();
        AliConfig config = PluginManager.getInstance().commonRegistry.getConfiguration();

        Map<ResourceLocation, LootTable> lootTables = collectLootTables(manager);
        Map<ResourceLocation, IDataNode> lootNodes = new HashMap<>();
        Map<ResourceLocation, LootTable> unprocessedLootTables = new HashMap<>(lootTables);
        Map<ResourceLocation, LootTable> fakeLootTables = new HashMap<>(fakeLootDataManager.getLootTables());
        List<ILootModifier<?>> lootModifiers = serverRegistry.getLootModifiers();
        Map<ILootModifier.IType<?>, List<ILootModifier<?>>> groupedTypes = lootModifiers.stream().collect(Collectors.groupingBy(ILootModifier::getType));
        List<ILootModifier<?>> blockLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.BLOCK, Collections.emptyList());
        List<ILootModifier<?>> entityLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.ENTITY, Collections.emptyList());
        List<ILootModifier<?>> lootTableLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.LOOT_TABLE, Collections.emptyList());
        List<ILootModifier<?>> unboundedLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.UNBOUNDED, Collections.emptyList());
        Set<ILootModifier<?>> attachedLootModifiers = Collections.newSetFromMap(new IdentityHashMap<>());
        Map<ResourceLocation, IDataNode> tradeNodes;

        lootTables.forEach(serverRegistry::addLootTable); // used for table references

        Set<ResourceLocation> referencedLootTables = collectReferencedLootTables(serverRegistry, lootTables, fakeLootTables);

        chunks.clear();

        // apply modifiers
        lootNodes.putAll(processBlocks(serverRegistry, config, unprocessedLootTables, fakeLootTables, blockLootModifiers, lootTableLootModifiers, unboundedLootModifiers, attachedLootModifiers));
        lootNodes.putAll(processEntities(serverRegistry, config, serverRegistry.getServerLevel(), unprocessedLootTables, fakeLootTables, entityLootModifiers, lootTableLootModifiers, unboundedLootModifiers, attachedLootModifiers, referencedLootTables));
        lootNodes.putAll(processLootTables(serverRegistry, config, unprocessedLootTables, fakeLootTables, lootTableLootModifiers, unboundedLootModifiers, attachedLootModifiers));

        if (lootModifiers.size() > attachedLootModifiers.size()) {
            LOGGER.info("{} of {} loot modifiers ended up on no page", lootModifiers.size() - attachedLootModifiers.size(), lootModifiers.size());
        }

        lootNodes = removeEmptyLootTable(serverRegistry, lootNodes);
        tradeNodes = new HashMap<>(processTrades(serverRegistry, config));

        LOGGER.info("Processing {} loot tables, {} fake loot tables and {} trades took {}ms", lootNodes.size(), fakeLootTables.size(), tradeNodes.size(), System.currentTimeMillis() - startTime);

        ByteBuf rawBuf = Unpooled.buffer();
        FriendlyByteBuf buf = new FriendlyByteBuf(rawBuf);

        // storing and compressing data
        serverRegistry.getTooltipCache().encode(serverRegistry, buf);
        writeLootData(serverRegistry, buf, lootNodes);
        NetworkUtils.writeMapData(Utils.MOD_ID, serverRegistry, buf, tradeNodes);

        NetworkUtils.compressAndStoreData(Utils.MOD_ID, rawBuf, (i, data) -> chunks.add(new LootDataChunkMessage(i, data)));

        serverRegistry.printRuntimeInfo();

        fakeLootDataManager.clearLootTables();
        serverRegistry.clearLootTables(); // not needed anymore
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

    /**
     * The items a table produces are collected here only to tell an empty table from a real one - the client derives
     * its own from the very same node tree ({@code GenericUtils.collectItems}), so nothing item-shaped is sent.
     */
    @NotNull
    private static Map<ResourceLocation, IDataNode> removeEmptyLootTable(AliServerRegistry serverRegistry, Map<ResourceLocation, IDataNode> lootNodes) {
        Map<ResourceLocation, IDataNode> result = new HashMap<>();
        int emptyLootTables = 0;
        int injectedLootTables = 0;

        for (Map.Entry<ResourceLocation, IDataNode> entry : lootNodes.entrySet()) {
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
    private static Map<ResourceLocation, LootTable> collectLootTables(LootDataManager manager) {
        Map<ResourceLocation, LootTable> lootTables = new HashMap<>();
        manager.getKeys(LootDataType.TABLE).forEach((location) -> lootTables.put(location, manager.getLootTable(location)));
        return lootTables;
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> processBlocks(AliServerRegistry serverRegistry, AliConfig config, Map<ResourceLocation, LootTable> lootTables,
                                                                  Map<ResourceLocation, LootTable> fakeLootTables, List<ILootModifier<?>> blockLootModifiers,
                                                                  List<ILootModifier<?>> lootTableLootModifiers, List<ILootModifier<?>> unboundedLootModifiers,
                                                                  Set<ILootModifier<?>> attachedLootModifiers) {
        Map<ResourceLocation, IDataNode> lootNodes = new HashMap<>();
        Map<ResourceLocation, List<Block>> blocksByLootTable = new LinkedHashMap<>();
        int defaultDropLootTables = 0;

        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation location = Utils.getLootTableKey(block);

            //noinspection ConstantValue
            if (location != null) {
                blocksByLootTable.computeIfAbsent(location, (k) -> new ArrayList<>()).add(block);
            }
        }

        // blocks sharing a single loot table (vanilla's dropsLike, e.g. wall banner -> banner) all contribute their
        // loot modifiers to the one node stored under that table's id, instead of the first block claiming it
        for (Map.Entry<ResourceLocation, List<Block>> entry : blocksByLootTable.entrySet()) {
            ResourceLocation location = entry.getKey();
            List<Block> blocks = entry.getValue().stream()
                    .filter((b) -> config.blockCategories.stream().filter((f) -> f.validate(b)).findFirst().map((f) -> !f.isHidden()).orElse(false))
                    .toList();
            LootTable lootTable = lootTables.remove(location);

            TooltipContext.set(location);

            if (!blocks.isEmpty()) {
                List<ILootModifier<?>> lootModifiers = filterByItems(serverRegistry, Stream.concat(Stream.concat(
                        blockLootModifiers.stream().filter((m) -> blocks.stream().anyMatch((b) -> predicateModifier(m, b))),
                        lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location))
                ), unboundedLootModifiers.stream()).toList(), lootTable, location, fakeLootTables, attachedLootModifiers);

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
                        } else if (blocks.stream().noneMatch((b) -> b.getLootTable().equals(BuiltInLootTables.EMPTY))) {
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

    private static boolean isDefaultBlockDrop(AliServerRegistry serverRegistry, AliConfig config, Block block, @Nullable LootTable lootTable) {
        if (lootTable == null || !isIgnoredFunctions(config, lootTable.functions)) {
            return false;
        }

        // mods inject entry-less pools into every block table, keeping them here would match no table at all
        List<LootPool> pools = serverRegistry.getLootPools(lootTable).stream().filter((p) -> p.entries.length > 0).toList();

        if (pools.size() != 1) {
            return false;
        }

        LootPool pool = pools.get(0);

        if (pool.entries.length != 1 || !isIgnoredFunctions(config, pool.functions) || !isIgnoredConditions(config, pool.conditions)
                || !isConstant(serverRegistry, pool.rolls, 1) || !isConstant(serverRegistry, pool.bonusRolls, 0)) {
            return false;
        }

        if (!(pool.entries[0] instanceof LootItem lootItem) || lootItem.item != block.asItem() || !isIgnoredFunctions(config, lootItem.functions)) {
            return false;
        }

        return isIgnoredConditions(config, lootItem.conditions);
    }

    private static boolean isIgnoredFunctions(AliConfig config, LootItemFunction[] functions) {
        return Arrays.stream(functions).allMatch((f) -> config.defaultBlockLootFunctions.contains(BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(f.getType())));
    }

    private static boolean isIgnoredConditions(AliConfig config, LootItemCondition[] conditions) {
        return Arrays.stream(conditions).allMatch((c) -> config.defaultBlockLootConditions.contains(BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(c.getType())));
    }

    private static boolean isConstant(AliServerRegistry serverRegistry, NumberProvider numberProvider, float value) {
        RangeValue range = serverRegistry.convertNumber(serverRegistry, numberProvider);

        return !range.isUnknown() && range.min() == value && range.max() == value;
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> processEntities(AliServerRegistry serverRegistry, AliConfig config, ServerLevel level, Map<ResourceLocation, LootTable> lootTables,
                                                                    Map<ResourceLocation, LootTable> fakeLootTables, List<ILootModifier<?>> entityLootModifiers,
                                                                    List<ILootModifier<?>> lootTableLootModifiers, List<ILootModifier<?>> unboundedLootModifiers,
                                                                    Set<ILootModifier<?>> attachedLootModifiers, Set<ResourceLocation> referencedLootTables) {
        Map<ResourceLocation, IDataNode> lootNodes = new HashMap<>();
        EntityLootTableResolver resolver = new EntityLootTableResolver(serverRegistry, level, referencedLootTables);
        Set<ResourceLocation> candidates = new HashSet<>(lootTables.keySet());
        Set<ResourceLocation> disabledLootTables = new HashSet<>();

        candidates.addAll(fakeLootTables.keySet());

        if (!entityLootModifiers.isEmpty()) {
            // a modifier can add drops to an entity whose own loot table does not exist, so those tables have to be
            // considered even though no data references them
            BuiltInRegistries.ENTITY_TYPE.forEach((entityType) -> candidates.add(entityType.getDefaultLootTable()));
        }

        // same grouping as for blocks: all entities sharing a loot table contribute to the single node under its id
        for (Map.Entry<ResourceLocation, List<EntityType<?>>> entry : resolver.resolveAll(candidates).entrySet()) {
            ResourceLocation location = entry.getKey();

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
                List<ILootModifier<?>> lootModifiers = filterByItems(serverRegistry, Stream.concat(Stream.concat(
                        // the only step that still needs an instance, and only when global loot modifiers exist at all
                        entityLootModifiers.stream().filter((m) -> entityTypes.stream().anyMatch((t) -> sampleEntities(resolver, t, location).stream().anyMatch((e) -> predicateModifier(m, e)))),
                        lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location))
                ), unboundedLootModifiers.stream()).toList(), lootTable, location, fakeLootTables, attachedLootModifiers);

                try {
                    if (lootTable != null) {
                        IDataNode node = serverRegistry.parseTable(lootModifiers, lootTable);
                        List<IDataNode> fakePools = getFakeLootPools(location, serverRegistry, fakeLootTables);

                        if (node instanceof LootTableNode lootTableNode) {
                            fakePools.forEach(lootTableNode::addChildren);
                        }

                        lootNodes.put(location, asEntityNode(node, entityTypes));
                    } else if (!lootModifiers.isEmpty()) {
                        IDataNode node = serverRegistry.parseTable(lootModifiers);
                        List<IDataNode> fakePools = getFakeLootPools(location, serverRegistry, fakeLootTables);

                        if (node instanceof LootTableNode lootTableNode) {
                            fakePools.forEach(lootTableNode::addChildren);
                        }

                        lootNodes.put(location, asEntityNode(node, entityTypes));
                    } else {
                        LootTable fakeLootTable = fakeLootTables.get(location);

                        if (fakeLootTable != null) {
                            lootNodes.put(location, asEntityNode(serverRegistry.parseTable(Collections.emptyList(), fakeLootTable), entityTypes));
                        } else if (!location.equals(BuiltInLootTables.EMPTY)) {
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

    private static void writeLootData(AliServerRegistry serverRegistry, FriendlyByteBuf buf, Map<ResourceLocation, IDataNode> lootNodes) {
        int countIndex = buf.writerIndex();
        int successfulNodes = 0;

        buf.writeInt(lootNodes.size());

        for (Map.Entry<ResourceLocation, IDataNode> entry : lootNodes.entrySet()) {
            int startOfEntry = buf.writerIndex();

            buf.writeResourceLocation(entry.getKey());
            buf.writeResourceLocation(entry.getValue().getId());

            if (NetworkUtils.writeNodeData(Utils.MOD_ID, serverRegistry, buf, entry.getKey(), entry.getValue())) {
                successfulNodes++;
            } else {
                buf.writerIndex(startOfEntry);
            }
        }

        if (successfulNodes != lootNodes.size()) {
            LOGGER.warn("Only {} of {} node(s) were encoded successfully", successfulNodes, lootNodes.size());

            int endIndex = buf.writerIndex();

            buf.writerIndex(countIndex);
            buf.writeInt(successfulNodes);
            buf.writerIndex(endIndex);
        }

        lootNodes.clear();
    }

    @NotNull
    private static List<Entity> sampleEntities(EntityLootTableResolver resolver, EntityType<?> type, ResourceLocation lootTable) {
        List<Entity> entities = resolver.getEntities(type);
        List<Entity> variants = entities.stream().filter((e) -> e instanceof Mob mob && lootTable.equals(mob.getLootTable())).toList();

        // a table claimed by the entityLootTables configuration has an id no instance of the type reports, so testing
        // the modifier against nothing would silently drop it
        return variants.isEmpty() ? entities : variants;
    }

    @NotNull
    private static IDataNode asEntityNode(IDataNode node, List<EntityType<?>> entityTypes) {
        if (node instanceof LootTableNode lootTableNode) {
            return new EntityLootTableNode(lootTableNode, entityTypes.get(0));
        }

        return node;
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> processLootTables(AliServerRegistry serverRegistry, AliConfig config, Map<ResourceLocation, LootTable> lootTables,
                                                                      Map<ResourceLocation, LootTable> fakeLootTables, List<ILootModifier<?>> lootTableLootModifiers,
                                                                      List<ILootModifier<?>> unboundedLootModifiers, Set<ILootModifier<?>> attachedLootModifiers) {
        Map<ResourceLocation, IDataNode> lootNodes = new HashMap<>();

        for (Map.Entry<ResourceLocation, LootTable> entry : lootTables.entrySet()) {
            ResourceLocation location = entry.getKey();

            TooltipContext.set(location);

            if (config.gameplayCategories.stream().filter((f) -> f.validate(location)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                LootTable lootTable = entry.getValue();
                List<ILootModifier<?>> lootModifiers = filterByItems(serverRegistry, Stream.concat(
                        lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location)),
                        unboundedLootModifiers.stream()
                ).toList(), lootTable, location, fakeLootTables, attachedLootModifiers);

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
    private static Map<ResourceLocation, IDataNode> processTrades(AliServerRegistry serverRegistry, AliConfig config) {
        Map<ResourceLocation, IDataNode> nodes = new HashMap<>();

        for (Map.Entry<ResourceLocation, AliServerRegistry.Trades> entry : serverRegistry.getTrades().entrySet()) {
            ResourceLocation location = entry.getKey();

            TooltipContext.set(location);

            if (config.tradeCategories.stream().filter((f) -> f.validate(location)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                try {
                    Int2ObjectMap<VillagerTrades.ItemListing[]> itemListingMap = entry.getValue().itemListings().get();

                    if (itemListingMap != null && itemListingMap.int2ObjectEntrySet().stream().anyMatch((e) -> e.getValue().length > 0)) {
                        nodes.put(location, serverRegistry.parseTrade(entry.getValue()));
                    } else {
                        LOGGER.warn("No trades defined for {}", location);
                    }
                } catch (Throwable e) {
                    LOGGER.warn("Failed to parse trade for {} with error {}", location, e.getMessage(), e);
                }
            }

            TooltipContext.clear();
        }

        return nodes;
    }

    private static <T> boolean predicateModifier(ILootModifier<?> modifier, T value) {
        try {
            //noinspection unchecked
            return ((ILootModifier<T>) modifier).predicate(value);
        } catch (Throwable e) {
            LOGGER.warn("Failed to evaluate loot modifier predicate for {}: {}", value, e.getMessage(), e);
            return false;
        }
    }

    @NotNull
    private static List<ILootModifier<?>> filterByItems(AliServerRegistry serverRegistry, List<ILootModifier<?>> modifiers, @Nullable LootTable lootTable,
                                                        ResourceLocation location, Map<ResourceLocation, LootTable> fakeLootTables,
                                                        Set<ILootModifier<?>> attachedLootModifiers) {
        if (modifiers.isEmpty()) {
            return modifiers;
        }

        List<ItemStack> items = collectProducibleItems(serverRegistry, lootTable, location, fakeLootTables);
        List<ILootModifier<?>> matched = modifiers.stream().filter((m) -> matchesItems(m, items)).toList();

        attachedLootModifiers.addAll(matched);
        return matched;
    }

    private static boolean matchesItems(ILootModifier<?> modifier, List<ItemStack> items) {
        ItemMatch match = predicateItem(modifier, items);

        if (modifier.getType() == ILootModifier.IType.UNBOUNDED) {
            return match == ItemMatch.MATCHED;
        }

        return match != ItemMatch.NOT_MATCHED;
    }

    @NotNull
    private static List<ItemStack> collectProducibleItems(AliServerRegistry serverRegistry, @Nullable LootTable lootTable, ResourceLocation location,
                                                          Map<ResourceLocation, LootTable> fakeLootTables) {
        List<ItemStack> items = new ArrayList<>();

        try {
            if (lootTable != null) {
                items.addAll(collectItems(serverRegistry.parseTable(Collections.emptyList(), lootTable)));
            }

            getFakeLootPools(location, serverRegistry, fakeLootTables).forEach((node) -> items.addAll(collectItems(node)));
        } catch (Throwable e) {
            LOGGER.warn("Failed to collect items of loot table {} with error {}", location, e.getMessage(), e);
        }

        return items;
    }

    private static ItemMatch predicateItem(ILootModifier<?> modifier, List<ItemStack> items) {
        if (items.isEmpty()) {
            return ItemMatch.NO_CANDIDATES;
        }

        try {
            List<IOperation> operations = modifier.getOperations();

            if (items.stream().anyMatch((i) -> operations.stream().anyMatch((o) -> o.predicate().test(i)))) {
                return ItemMatch.MATCHED;
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to evaluate loot modifier operation predicate: {}", e.getMessage(), e);
            return ItemMatch.NO_CANDIDATES;
        }

        return ItemMatch.NOT_MATCHED;
    }

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

        return itemStacks;
    }

    @NotNull
    private static Set<ResourceLocation> collectReferencedLootTables(AliServerRegistry serverRegistry, Map<ResourceLocation, LootTable> lootTables,
                                                                    Map<ResourceLocation, LootTable> fakeLootTables) {
        Set<ResourceLocation> referenced = new HashSet<>();

        Stream.concat(lootTables.values().stream(), fakeLootTables.values().stream())
                .forEach((lootTable) -> serverRegistry.getLootPools(lootTable).forEach((pool) -> collectReferences(pool.entries, referenced)));

        return referenced;
    }

    private static void collectReferences(LootPoolEntryContainer[] entries, Set<ResourceLocation> referenced) {
        for (LootPoolEntryContainer entry : entries) {
            if (entry instanceof LootTableReference reference) {
                referenced.add(reference.name);
            } else if (entry instanceof CompositeEntryBase composite) {
                collectReferences(composite.children, referenced);
            }
        }
    }

    private static List<IDataNode> getFakeLootPools(ResourceLocation location, AliServerRegistry serverRegistry, Map<ResourceLocation, LootTable> fakeLootTables) {
        LootTable fakeLootTable = fakeLootTables.get(location);

        if (fakeLootTable != null) {
            IDataNode fakeNode = serverRegistry.parseTable(Collections.emptyList(), fakeLootTable);

            if (fakeNode instanceof LootTableNode fakeLootTableNode) {
                return fakeLootTableNode.nodes();
            }
        }

        return Collections.emptyList();
    }

    private enum ItemMatch {
        MATCHED,
        NOT_MATCHED,
        NO_CANDIDATES
    }
}
