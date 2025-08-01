package com.yanny.ali.network;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.manager.AliServerRegistry;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.server.ItemCollectorUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractServer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<SyncLootTableMessage> messages = new LinkedList<>();

    public final void readLootTables(ReloadableServerRegistries.Holder manager, ServerLevel level) {
        AliServerRegistry serverRegistry = PluginManager.SERVER_REGISTRY;
        Map<ResourceKey<LootTable>, LootTable> lootTables = collectLootTables(manager);
        Map<ResourceKey<LootTable>, IDataNode> lootNodes = new HashMap<>();
        Map<ResourceKey<LootTable>, LootTable> unprocessedLootTables = new HashMap<>(lootTables);
        Map<ResourceKey<LootTable>, List<Item>> lootTableItems;
        Map<ResourceKey<LootTable>, List<ItemStack>> lootTableItemStacks;
        List<ILootModifier<?>> lootModifiers = serverRegistry.getLootModifiers();
        Map<ILootModifier.IType<?>, List<ILootModifier<?>>> groupedTypes = lootModifiers.stream().collect(Collectors.groupingBy(ILootModifier::getType));
        List<ILootModifier<?>> blockLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.BLOCK, Collections.emptyList());
        List<ILootModifier<?>> entityLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.ENTITY, Collections.emptyList());
        List<ILootModifier<?>> lootTableLootModifiers = groupedTypes.getOrDefault(ILootModifier.IType.LOOT_TABLE, Collections.emptyList());

        serverRegistry.setServerLevel(level);
        lootTables.forEach(serverRegistry::addLootTable);
        lootTableItems = lootTables.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, AbstractServer::getItems));
        messages.clear();

        // apply modifiers
        lootNodes.putAll(processBlocks(serverRegistry, unprocessedLootTables, blockLootModifiers, lootTableLootModifiers, lootTableItems));
        lootNodes.putAll(processEntities(serverRegistry, level, unprocessedLootTables, entityLootModifiers, lootTableLootModifiers, lootTableItems));
        lootNodes.putAll(processLootTables(serverRegistry, unprocessedLootTables, lootTableLootModifiers, lootTableItems));

        lootTableItemStacks = lootNodes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> collectItems(e.getValue())));
        lootTables = removeEmptyLootTable(lootTables, lootTableItemStacks);

        sendLootData(lootTables, lootTableItemStacks, lootNodes);

        serverRegistry.printRuntimeInfo();
    }

    public final void syncLootTables(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            sendClearMessage(serverPlayer, new ClearMessage());

            for (SyncLootTableMessage message : messages) {
                try {
                    sendSyncMessage(serverPlayer, message);
                } catch (Throwable e) {
                    LOGGER.warn("Failed to send message for loot table {} with error: {}", message.location().location(), e.getMessage());
                }
            }

            sendDoneMessage(serverPlayer, new DoneMessage());
        }
    }

    protected abstract void sendClearMessage(ServerPlayer serverPlayer, ClearMessage message);

    protected abstract void sendSyncMessage(ServerPlayer serverPlayer, SyncLootTableMessage message);

    protected abstract void sendDoneMessage(ServerPlayer serverPlayer, DoneMessage message);

    @NotNull
    private static List<Item> getItems(Map.Entry<ResourceKey<LootTable>, LootTable> lootTableMap) {
        return ItemCollectorUtils.collectLootTable(PluginManager.SERVER_REGISTRY, lootTableMap.getValue());
    }

    @NotNull
    private static Map<ResourceKey<LootTable>, LootTable> removeEmptyLootTable(Map<ResourceKey<LootTable>, LootTable> lootTables, Map<ResourceKey<LootTable>, List<ItemStack>> items) {
        Map<ResourceKey<LootTable>, LootTable> result = new HashMap<>();

        for (Map.Entry<ResourceKey<LootTable>, LootTable> entry : lootTables.entrySet()) {
            if (!items.getOrDefault(entry.getKey(), Collections.emptyList()).isEmpty()) {
                result.put(entry.getKey(), entry.getValue());
            } else {
                LOGGER.info("Skipping empty loot table {}", entry.getKey());
            }
        }

        return result;
    }

    @NotNull
    private static Map<ResourceKey<LootTable>, LootTable> collectLootTables(ReloadableServerRegistries.Holder manager) {
        Map<ResourceKey<LootTable>, LootTable> lootTables = new HashMap<>();

        manager.get().lookup(Registries.LOOT_TABLE).ifPresent(
                (lookup) -> lookup.listElements().forEach((reference) -> lootTables.put(reference.key(), reference.value()))
        );
        return lootTables;
    }

    @NotNull
    private static Map<ResourceKey<LootTable>, IDataNode> processBlocks(AliServerRegistry serverRegistry, Map<ResourceKey<LootTable>, LootTable> lootTables,
                                                                  List<ILootModifier<?>> blockLootModifiers, List<ILootModifier<?>> lootTableLootModifiers,
                                                                  Map<ResourceKey<LootTable>, List<Item>> lootTableItems) {
        Map<ResourceKey<LootTable>, IDataNode> lootNodes = new HashMap<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceKey<LootTable> location = block.getLootTable();
            LootTable lootTable = lootTables.remove(location);
            List<Item> items = lootTableItems.get(location);

            if (lootTable != null) {
                List<ILootModifier<?>> lootModifiers = Stream.concat(
                        blockLootModifiers.stream().filter((m) -> predicateModifier(m, block, items)),
                        lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items))
                ).toList();

                lootNodes.put(location, serverRegistry.parseTable(lootModifiers, lootTable));
            } else {
                LOGGER.debug("Missing block loot table for {}", block);
            }
        }

        return lootNodes;
    }

    @NotNull
    private static Map<ResourceKey<LootTable>, IDataNode> processEntities(AliServerRegistry serverRegistry, ServerLevel level, Map<ResourceKey<LootTable>, LootTable> lootTables,
                                                                    List<ILootModifier<?>> entityLootModifiers, List<ILootModifier<?>> lootTableLootModifiers,
                                                                    Map<ResourceKey<LootTable>, List<Item>> lootTableItems) {
        Map<ResourceKey<LootTable>, IDataNode> lootNodes = new HashMap<>();

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            List<Entity> entityList = serverRegistry.createEntities(entityType, level);

            for (Entity entity : entityList) {
                if (entity instanceof Mob mob) {
                    ResourceKey<LootTable> location = mob.getLootTable();
                    LootTable lootTable = lootTables.remove(location);
                    List<Item> items = lootTableItems.get(location);

                    if (lootTable != null) {
                        List<ILootModifier<?>> lootModifiers = Stream.concat(
                                entityLootModifiers.stream().filter((m) -> predicateModifier(m, entity, items)),
                                lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items))
                        ).toList();

                        lootNodes.put(location, serverRegistry.parseTable(lootModifiers, lootTable));
                    } else {
                        LOGGER.debug("Missing entity loot table for {}", entity);
                    }
                }
            }
        }

        return lootNodes;
    }

    @NotNull
    private static Map<ResourceKey<LootTable>, IDataNode> processLootTables(AliServerRegistry serverRegistry, Map<ResourceKey<LootTable>, LootTable> lootTables,
                                                                      List<ILootModifier<?>> lootTableLootModifiers, Map<ResourceKey<LootTable>, List<Item>> lootTableItems) {
        Map<ResourceKey<LootTable>, IDataNode> lootNodes = new HashMap<>();

        for (Map.Entry<ResourceKey<LootTable>, LootTable> entry : lootTables.entrySet()) {
            ResourceKey<LootTable> location = entry.getKey();
            LootTable lootTable = entry.getValue();
            List<Item> items = lootTableItems.get(location);
            List<ILootModifier<?>> lootModifiers = lootTableLootModifiers.stream().filter((m) -> predicateModifier(m, location, items)).toList();

            lootNodes.put(location, serverRegistry.parseTable(lootModifiers, lootTable));
        }

        return lootNodes;
    }

    private static <T> boolean predicateModifier(ILootModifier<?> modifier, T value, List<Item> items) {
        //noinspection unchecked
        return ((ILootModifier<T>) modifier).predicate(value) && predicateItem(modifier, items);
    }

    private static boolean predicateItem(ILootModifier<?> modifier, List<Item> items) { //FIXME ItemStack!
        return items.stream().anyMatch((i) -> modifier.getOperations().stream().anyMatch(o -> o.predicate().test(i.getDefaultInstance())));
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

    private void sendLootData(Map<ResourceKey<LootTable>, LootTable> lootTables, Map<ResourceKey<LootTable>, List<ItemStack>> lootTableItemStacks, Map<ResourceKey<LootTable>, IDataNode> lootNodes) {
        lootTables.forEach((location, lootTable) -> messages.add(new SyncLootTableMessage(location, lootTableItemStacks.getOrDefault(location, Collections.emptyList()), lootNodes.get(location))));
    }

    private static List<ItemStack> toItemStacks(TagKey<Item> tag) {
        return BuiltInRegistries.ITEM.getTag(tag)
                .map(holders -> holders.stream().map(Holder::value).map(Item::getDefaultInstance).toList())
                .orElse(Collections.emptyList());
    }
}
