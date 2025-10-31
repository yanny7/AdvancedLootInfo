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
import com.yanny.ali.plugin.server.ItemCollectorUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractServer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<SyncLootTableMessage> lootTableMessages = new LinkedList<>();
    private final List<SyncTradeMessage> tradeMessages = new LinkedList<>();

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
        IDataNode wanderingTraderNode = processWanderingTrader(serverRegistry);

        serverRegistry.setServerLevel(level);
        lootTables.forEach(serverRegistry::addLootTable); // used for table references
        lootTableItems = lootTables.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, AbstractServer::getItems));

        lootTableMessages.clear();
        tradeMessages.clear();

        // apply modifiers
        lootNodes.putAll(processBlocks(serverRegistry, config, unprocessedLootTables, blockLootModifiers, lootTableLootModifiers, lootTableItems));
        lootNodes.putAll(processEntities(serverRegistry, config, level, unprocessedLootTables, entityLootModifiers, lootTableLootModifiers, lootTableItems));
        lootNodes.putAll(processLootTables(serverRegistry, config, unprocessedLootTables, lootTableLootModifiers, lootTableItems));

        lootTableItemStacks = lootNodes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> collectItems(e.getValue())));
        lootTables = removeEmptyLootTable(lootTables, lootTableItemStacks);
        tradeNodes = new HashMap<>(processTrades(serverRegistry, config, tradeItems));

        sendLootData(lootTables, lootTableItemStacks, lootNodes);
        sendTradeData(tradeNodes, tradeItems, wanderingTraderNode, wanderingTraderItems);

        serverRegistry.clearLootTables(); // not needed anymore
        serverRegistry.printRuntimeInfo();
        LOGGER.info("Processing {} loot tables and {} trades took {}ms", lootTables.size(), tradeNodes.size() + 1, System.currentTimeMillis() - startTime);
    }

    public final void syncLootTables(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            LOGGER.info("Started syncing loot info to {}", player.getScoreboardName());
            sendClearMessage(serverPlayer, new ClearMessage(lootTableMessages.size() + tradeMessages.size()));

            for (SyncLootTableMessage message : lootTableMessages) {
                try {
                    sendSyncLootTableMessage(serverPlayer, message);
                } catch (Throwable e) {
                    e.printStackTrace();
                    LOGGER.warn("Failed to send message for loot table {} with error: {}", message.location(), e.getMessage());
                }
            }

            for (SyncTradeMessage message : tradeMessages) {
                try {
                    sendSyncTradeMessage(serverPlayer, message);
                } catch (Throwable e) {
                    e.printStackTrace();
                    LOGGER.warn("Failed to send message for trade {} with error: {}", message.location(), e.getMessage());
                }
            }

            sendDoneMessage(serverPlayer, new DoneMessage());
            LOGGER.info("Finished syncing loot info to {}", player.getScoreboardName());
        }
    }

    protected abstract void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message);

    protected abstract void sendSyncLootTableMessage(ServerPlayer serverPlayer, SyncLootTableMessage message);

    protected abstract void sendSyncTradeMessage(ServerPlayer serverPlayer, SyncTradeMessage message);

    protected abstract void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message);

    @NotNull
    private static List<Item> getItems(Map.Entry<ResourceLocation, LootTable> lootTableMap) {
        return ItemCollectorUtils.collectLootTable(PluginManager.SERVER_REGISTRY, lootTableMap.getValue());
    }

    @NotNull
    private static Map<ResourceLocation, LootTable> removeEmptyLootTable(Map<ResourceLocation, LootTable> lootTables, Map<ResourceLocation, List<ItemStack>> items) {
        Map<ResourceLocation, LootTable> result = new HashMap<>();
        int emptyLootTables = 0;

        for (Map.Entry<ResourceLocation, LootTable> entry : lootTables.entrySet()) {
            if (!items.getOrDefault(entry.getKey(), Collections.emptyList()).isEmpty()) {
                result.put(entry.getKey(), entry.getValue());
            } else {
                emptyLootTables++;
            }
        }

        LOGGER.info("Skipped {} empty or hidden loot tables", emptyLootTables);
        return result;
    }

    @NotNull
    private static Map<ResourceLocation, LootTable> collectLootTables(ReloadableServerRegistries.Holder manager) {
        Map<ResourceLocation, LootTable> lootTables = new HashMap<>();
        Registry<LootTable> registry = (Registry<LootTable>)manager.lookup().lookup(Registries.LOOT_TABLE).orElseThrow();

        registry.entrySet().forEach((e) -> lootTables.put(e.getKey().location(), e.getValue()));

        return lootTables;
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> processBlocks(AliServerRegistry serverRegistry, AliConfig config, Map<ResourceLocation, LootTable> lootTables,
                                                                  List<ILootModifier<?>> blockLootModifiers, List<ILootModifier<?>> lootTableLootModifiers,
                                                                  Map<ResourceLocation, List<Item>> lootTableItems) {
        Map<ResourceLocation, IDataNode> lootNodes = new HashMap<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            block.getLootTable().ifPresent((resourceKey) -> {
                ResourceLocation location = resourceKey.location();
                LootTable lootTable = lootTables.remove(location);

                if (config.blockCategories.stream().filter((f) -> f.validate(block)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                    List<Item> items = lootTableItems.get(location);

                    if (lootTable != null && items != null) {
                        List<ILootModifier<?>> lootModifiers = Stream.concat(
                                blockLootModifiers.stream().filter((m) -> predicateModifier(m, block, items)),
                                lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items))
                        ).toList();

                        try {
                            lootNodes.put(location, serverRegistry.parseTable(lootModifiers, lootTable));
                        } catch (Throwable e) {
                            e.printStackTrace();
                            LOGGER.warn("Failed to parse block loot table {} with error {}", location, e.getMessage());
                        }
                    } else {
                        LOGGER.debug("Missing block loot table for {}", block);
                    }
                } else {
                    lootTables.remove(location);
                }
            });
        }

        return lootNodes;
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> processEntities(AliServerRegistry serverRegistry, AliConfig config, ServerLevel level, Map<ResourceLocation, LootTable> lootTables,
                                                                    List<ILootModifier<?>> entityLootModifiers, List<ILootModifier<?>> lootTableLootModifiers,
                                                                    Map<ResourceLocation, List<Item>> lootTableItems) {
        Map<ResourceLocation, IDataNode> lootNodes = new HashMap<>();

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            List<Entity> entityList = serverRegistry.createEntities(entityType, level);

            for (Entity entity : entityList) {
                if (entity instanceof Mob mob) {
                    mob.getLootTable().ifPresent((resourceKey) -> {
                        ResourceLocation location = resourceKey.location();
                        LootTable lootTable = lootTables.remove(location);

                        if (config.entityCategories.stream().filter((f) -> f.validate(entityType)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                            List<Item> items = lootTableItems.get(location);

                            if (lootTable != null && items != null) {
                                List<ILootModifier<?>> lootModifiers = Stream.concat(
                                        entityLootModifiers.stream().filter((m) -> predicateModifier(m, entity, items)),
                                        lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items))
                                ).toList();

                                try {
                                    lootNodes.put(location, serverRegistry.parseTable(lootModifiers, lootTable));
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                    LOGGER.warn("Failed to parse entity loot table {} with error {}", location, e.getMessage());
                                }
                            } else {
                                LOGGER.debug("Missing entity loot table for {}", entity);
                            }
                        }
                    });
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

            if (config.gameplayCategories.stream().filter((f) -> f.validate(location)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                LootTable lootTable = entry.getValue();
                List<Item> items = lootTableItems.get(location);
                List<ILootModifier<?>> lootModifiers = lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items)).toList();

                try {
                    lootNodes.put(location, serverRegistry.parseTable(lootModifiers, lootTable));
                } catch (Throwable e) {
                    e.printStackTrace();
                    LOGGER.warn("Failed to parse loot table {} with error {}", location, e.getMessage());
                }
            }
        }

        lootTables.clear();
        return lootNodes;
    }

    @NotNull
    private static Map<ResourceLocation, IDataNode> processTrades(AliServerRegistry serverRegistry, AliConfig config, Map<ResourceLocation, Pair<List<Item>, List<Item>>> tradeItems) {
        Map<ResourceLocation, IDataNode> nodes = new HashMap<>();

        for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : BuiltInRegistries.VILLAGER_PROFESSION.entrySet()) {
            ResourceLocation location = entry.getKey().location();

            if (config.tradeCategories.stream().filter((f) -> f.validate(location)).findFirst().map((f) -> !f.isHidden()).orElse(false)) {
                Int2ObjectMap<VillagerTrades.ItemListing[]> itemListingMap = VillagerTrades.TRADES.get(entry.getValue());

                if (itemListingMap != null && itemListingMap.int2ObjectEntrySet().stream().anyMatch((e) -> e.getValue().length > 0)) {
                    try {
                        nodes.put(location, serverRegistry.parseTrade(itemListingMap));
                        tradeItems.put(location, ItemCollectorUtils.collectTradeItems(serverRegistry, itemListingMap));
                    } catch (Throwable e) {
                        e.printStackTrace();
                        LOGGER.warn("Failed to parse trade for villager {} with error {}", entry.getValue().name(), e.getMessage());
                    }
                } else {
                    LOGGER.warn("No trades defined for {}", location);
                }
            }
        }

        return nodes;
    }

    @NotNull
    private static IDataNode processWanderingTrader(AliServerRegistry serverRegistry) {
        try {
            return serverRegistry.parseTrade(VillagerTrades.WANDERING_TRADER_TRADES);
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.warn("Failed to parse wandering trader with error {}", e.getMessage());
            return new MissingNode();
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

    private void sendLootData(Map<ResourceLocation, LootTable> lootTables, Map<ResourceLocation, List<ItemStack>> lootTableItemStacks, Map<ResourceLocation, IDataNode> lootNodes) {
        lootTables.forEach((location, lootTable) -> lootTableMessages.add(new SyncLootTableMessage(location, lootTableItemStacks.getOrDefault(location, Collections.emptyList()), lootNodes.get(location))));
    }

    private void sendTradeData(Map<ResourceLocation, IDataNode> trades, Map<ResourceLocation, Pair<List<Item>, List<Item>>> items, IDataNode wanderingTraderNode, Pair<List<Item>, List<Item>> wanderingTraderItems) {
        trades.forEach((location, node) -> tradeMessages.add(new SyncTradeMessage(location, node, items.get(location))));
        tradeMessages.add(new SyncTradeMessage(wanderingTraderNode, wanderingTraderItems));
    }

    private static <T extends ItemLike> List<ItemStack> toItemStacks(TagKey<T> tag) {
        Optional<? extends Holder.Reference<? extends Registry<?>>> registry = BuiltInRegistries.REGISTRY.get(tag.registry().location());

        if (registry.isPresent()) {
            //noinspection unchecked
            Holder.Reference<? extends Registry<T>> reference = (Holder.Reference<? extends Registry<T>>) registry.get();
            return reference
                    .value()
                    .get(tag)
                    .map(holders -> holders.stream().map(Holder::value).map((i) -> i.asItem().getDefaultInstance()).toList())
                    .orElse(Collections.emptyList());
        } else {
            return Collections.emptyList();
        }
    }
}
