package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;

public class AliServerRegistry implements IServerRegistry, IServerUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<LootPoolEntryType, BiFunction<IServerUtils, LootPoolEntryContainer, List<Item>>> entryItemCollectorMap = new HashMap<>();
    private final Map<LootItemFunctionType, TriFunction<IServerUtils, List<Item>, LootItemFunction, List<Item>>> functionItemCollectorMap = new HashMap<>();
    private final Map<LootNumberProviderType, BiFunction<IServerUtils, NumberProvider, RangeValue>> numberConverterMap = new HashMap<>();
    private final Map<LootPoolEntryType, EntryFactory<?>> entryFactoryMap = new HashMap<>();
    private final Map<LootItemFunctionType, BiFunction<IServerUtils, LootItemFunction, ITooltipNode>> functionTooltipMap = new HashMap<>();
    private final Map<LootItemConditionType, BiFunction<IServerUtils, LootItemCondition, ITooltipNode>> conditionTooltipMap = new HashMap<>();
    private final Map<LootItemConditionType, TriConsumer<IServerUtils, LootItemCondition, Map<Enchantment, Map<Integer, RangeValue>>>> chanceModifierMap = new HashMap<>();
    private final Map<LootItemFunctionType, TriConsumer<IServerUtils, LootItemFunction, Map<Enchantment, Map<Integer, RangeValue>>>> countModifierMap = new HashMap<>();
    private final Map<LootItemFunctionType, TriFunction<IServerUtils, LootItemFunction, ItemStack, ItemStack>> itemStackModifierMap = new HashMap<>();
    private final Map<ResourceLocation, LootTable> lootTableMap = new HashMap<>();
    private final List<ILootModifier<?>> lootModifierMap = new LinkedList<>();

    private ServerLevel serverLevel;
    private LootContext lootContext;

    public void addLootTable(ResourceLocation resourceLocation, LootTable lootTable) {
        lootTableMap.put(resourceLocation, lootTable);
    }

    public List<ILootModifier<?>> getLootModifiers() {
        return lootModifierMap;
    }

    public void setServerLevel(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
        this.lootContext = new LootContext(new LootParams(serverLevel, Map.of(), Map.of(), 0F), RandomSource.create(), new LootDataResolver() {
            @Override
            public @Nullable <T> T getElement(LootDataId<T> lootDataId) {
                return null;
            }
        });
    }

    @Override
    public <T extends LootPoolEntryContainer> void registerItemCollector(LootPoolEntryType type, BiFunction<IServerUtils, T, List<Item>> itemSupplier) {
        //noinspection unchecked
        entryItemCollectorMap.put(type, (u, e) -> itemSupplier.apply(u, (T) e));
    }

    @Override
    public <T extends LootItemFunction> void registerItemCollector(LootItemFunctionType type, TriFunction<IServerUtils, List<Item>, T, List<Item>> itemSupplier) {
        //noinspection unchecked
        functionItemCollectorMap.put(type, (u, l, f) -> itemSupplier.apply(u, l, (T) f));
    }

    @Override
    public <T extends LootPoolEntryContainer> void registerEntry(LootPoolEntryType type, EntryFactory<T> entryFactory) {
        entryFactoryMap.put(type, entryFactory);
    }

    @Override
    public <T extends LootItemFunction> void registerFunctionTooltip(LootItemFunctionType type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        functionTooltipMap.put(type, (u, f) -> getter.apply(u, (T) f));
    }

    @Override
    public <T extends LootItemCondition> void registerConditionTooltip(LootItemConditionType type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        conditionTooltipMap.put(type, (u, c) -> getter.apply(u, (T) c));
    }

    @Override
    public <T extends NumberProvider> void registerNumberProvider(LootNumberProviderType type, BiFunction<IServerUtils, T, RangeValue> converter) {
        //noinspection unchecked
        numberConverterMap.put(type, (u, t) -> converter.apply(u, (T) t));
    }

    @Override
    public <T extends LootItemFunction> void registerCountModifier(LootItemFunctionType type, TriConsumer<IServerUtils, T, Map<Enchantment, Map<Integer, RangeValue>>> consumer) {
        //noinspection unchecked
        countModifierMap.put(type, (u, f, v) -> consumer.accept(u, (T) f, v));
    }

    @Override
    public <T extends LootItemCondition> void registerChanceModifier(LootItemConditionType type, TriConsumer<IServerUtils, T, Map<Enchantment, Map<Integer, RangeValue>>> consumer) {
        //noinspection unchecked
        chanceModifierMap.put(type, (u, f, v) -> consumer.accept(u, (T) f, v));
    }

    @Override
    public <T extends LootItemFunction> void registerItemStackModifier(LootItemFunctionType type, TriFunction<IServerUtils, T, ItemStack, ItemStack> consumer) {
        //noinspection unchecked
        itemStackModifierMap.put(type, (u, f, i) -> consumer.apply(u, (T) f, i));
    }

    @Override
    public <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry) {
        BiFunction<IServerUtils, LootPoolEntryContainer, List<Item>> itemSupplier = entryItemCollectorMap.get(entry.getType());

        if (itemSupplier != null) {
            return itemSupplier.apply(utils, entry);
        } else {
            return List.of();
        }
    }

    @Override
    public <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function) {
        TriFunction<IServerUtils, List<Item>, LootItemFunction, List<Item>> itemSupplier = functionItemCollectorMap.get(function.getType());

        if (itemSupplier != null) {
            return itemSupplier.apply(utils, items, function);
        } else {
            return List.of();
        }
    }

    @Override
    public <T extends LootPoolEntryContainer> EntryFactory<T> getEntryFactory(IServerUtils utils, LootPoolEntryType type) {
        //noinspection unchecked
        EntryFactory<T> entryFactory = (EntryFactory<T>) entryFactoryMap.get(type);

        return Objects.requireNonNullElseGet(entryFactory, () -> (modifiers, utils1, entry, chance, sumWeight, functions, conditions) -> new MissingNode());
    }

    @Override
    public <T extends LootItemFunction> ITooltipNode getFunctionTooltip(IServerUtils utils, T function) {
        BiFunction<IServerUtils, LootItemFunction, ITooltipNode> entryTooltipGetter = functionTooltipMap.get(function.getType());

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, function);
        } else {
            LOGGER.warn("Function tooltip for {} was not registered", function.getClass().getCanonicalName());
            return TooltipNode.EMPTY;
        }
    }

    @Override
    public <T extends LootItemCondition> ITooltipNode getConditionTooltip(IServerUtils utils, T condition) {
        BiFunction<IServerUtils, LootItemCondition, ITooltipNode> entryTooltipGetter = conditionTooltipMap.get(condition.getType());

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, condition);
        } else {
            LOGGER.warn("Condition tooltip for {} was not registered", condition.getClass().getCanonicalName());
            return TooltipNode.EMPTY;
        }
    }

    @Override
    public <T extends LootItemFunction> void applyCountModifier(IServerUtils utils, T function, Map<Enchantment, Map<Integer, RangeValue>> count) {
        TriConsumer<IServerUtils, LootItemFunction, Map<Enchantment, Map<Integer, RangeValue>>> bonusCountConsumer = countModifierMap.get(function.getType());

        if (bonusCountConsumer != null) {
            bonusCountConsumer.accept(utils, function, count);
        }
    }

    @Override
    public <T extends LootItemCondition> void applyChanceModifier(IServerUtils utils, T condition, Map<Enchantment, Map<Integer, RangeValue>> chance) {
        TriConsumer<IServerUtils, LootItemCondition, Map<Enchantment, Map<Integer, RangeValue>>> bonusChanceConsumer = chanceModifierMap.get(condition.getType());

        if (bonusChanceConsumer != null) {
            bonusChanceConsumer.accept(utils, condition, chance);
        }
    }

    @Override
    public <T extends LootItemFunction> ItemStack applyItemStackModifier(IServerUtils utils, T function, ItemStack itemStack) {
        TriFunction<IServerUtils, LootItemFunction, ItemStack, ItemStack> bonusChanceConsumer = itemStackModifierMap.get(function.getType());

        if (bonusChanceConsumer != null) {
            itemStack = bonusChanceConsumer.apply(utils, function, itemStack);
        }

        return itemStack;
    }

    @Override
    public RangeValue convertNumber(IServerUtils utils, @Nullable NumberProvider numberProvider) {
        if (numberProvider != null) {
            BiFunction<IServerUtils, NumberProvider, RangeValue> function = numberConverterMap.get(numberProvider.getType());

            if (function != null) {
                try {
                    return function.apply(utils, numberProvider);
                } catch (Throwable throwable) {
                    LOGGER.warn("Failed to convert number with error {}", throwable.getMessage());
                }
            } else {
                LOGGER.warn("Number converter for {} was not registered", numberProvider.getClass().getCanonicalName());
            }
        }

        return new RangeValue(false, true);
    }

    @Nullable
    @Override
    public ServerLevel getServerLevel() {
        return serverLevel;
    }

    @Nullable
    @Override
    public LootContext getLootContext() {
        return lootContext;
    }

    @Nullable
    @Override
    public LootTable getLootTable(ResourceLocation resourceLocation) {
        return lootTableMap.get(resourceLocation);
    }

    @Override
    public List<LootPool> getLootPools(LootTable lootTable) {
        return Services.PLATFORM.getLootPools(lootTable);
    }

    public IDataNode parseTable(List<ILootModifier<?>> modifiers, LootTable lootTable) {
        return new LootTableNode(modifiers, this, lootTable);
    }

    public void printServerInfo() {
        LOGGER.info("Registered {} entry item collectors", entryItemCollectorMap.size());
        LOGGER.info("Registered {} function item collectors", functionItemCollectorMap.size());
        LOGGER.info("Registered {} number converters", numberConverterMap.size());
        LOGGER.info("Registered {} entry factories", entryFactoryMap.size());
        LOGGER.info("Registered {} function tooltips", functionTooltipMap.size());
        LOGGER.info("Registered {} condition tooltips", conditionTooltipMap.size());
        LOGGER.info("Registered {} chance modifiers", chanceModifierMap.size());
        LOGGER.info("Registered {} count modifiers", countModifierMap.size());
        LOGGER.info("Registered {} item stack modifiers", itemStackModifierMap.size());
        LOGGER.info("Registered {} loot tables", lootTableMap.size());
        LOGGER.info("Registered {} loot modifiers", lootModifierMap.size());
    }
}
