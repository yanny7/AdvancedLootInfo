package com.yanny.ali.manager;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AliServerRegistry implements IServerRegistry, IServerUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<Class<?>, BiFunction<IServerUtils, LootPoolEntryContainer, List<Item>>> entryItemCollectorMap = new HashMap<>();
    private final Map<Class<?>, TriFunction<IServerUtils, List<Item>, LootItemFunction, List<Item>>> functionItemCollectorMap = new HashMap<>();
    private final Map<Class<?>, BiFunction<IServerUtils, NumberProvider, RangeValue>> numberConverterMap = new HashMap<>();
    private final Map<Class<?>, EntryFactory<?>> entryFactoryMap = new HashMap<>();

    private final Map<Class<?>, BiFunction<IServerUtils, LootItemFunction, ITooltipNode>> functionTooltipMap = new HashMap<>();
    private final Map<Class<?>, BiFunction<IServerUtils, LootItemCondition, ITooltipNode>> conditionTooltipMap = new HashMap<>();
    private final Map<Class<?>, BiFunction<IServerUtils, Ingredient, ITooltipNode>> ingredientTooltipMap = new HashMap<>();
    private final Map<Class<?>, BiFunction<IServerUtils, ItemSubPredicate, ITooltipNode>> itemSubPredicateTooltipMap = new HashMap<>();
    private final Map<MapCodec<?>, BiFunction<IServerUtils, EntitySubPredicate, ITooltipNode>> entitySubPredicateTooltipMap = new HashMap<>();
    private final Map<DataComponentType<?>, BiFunction<IServerUtils, Object, ITooltipNode>> dataComponentTypeTooltipMap = new HashMap<>();
    private final Map<Class<?>, BiFunction<IServerUtils, ConsumeEffect, ITooltipNode>> consumeEffectTooltipMap = new HashMap<>();

    private final Map<Class<?>, TriConsumer<IServerUtils, LootItemCondition, Map<Holder<Enchantment>, Map<Integer, RangeValue>>>> chanceModifierMap = new HashMap<>();
    private final Map<Class<?>, TriConsumer<IServerUtils, LootItemFunction, Map<Holder<Enchantment>, Map<Integer, RangeValue>>>> countModifierMap = new HashMap<>();
    private final Map<Class<?>, TriFunction<IServerUtils, LootItemFunction, ItemStack, ItemStack>> itemStackModifierMap = new HashMap<>();

    private final Map<ResourceLocation, LootTable> lootTableMap = new HashMap<>();
    private final List<Function<IServerUtils, List<ILootModifier<?>>>> lootModifierGetters = new LinkedList<>();
    private final List<ILootModifier<?>> lootModifierMap = new LinkedList<>();

    private final Map<Class<?>, TriFunction<IServerUtils, VillagerTrades.ItemListing, List<ITooltipNode>, IDataNode>> itemListingFactoryMap = new HashMap<>();
    private final Map<Class<?>, BiFunction<IServerUtils, VillagerTrades.ItemListing, Pair<List<Item>, List<Item>>>> tradeItemCollectorMap = new HashMap<>();

    private final Set<Class<?>> missingEntryFactories = new HashSet<>();
    private final Set<Class<?>> missingFunctionTooltips = new HashSet<>();
    private final Set<Class<?>> missingConditionTooltips = new HashSet<>();
    private final Set<Class<?>> missingIngredientTooltips = new HashSet<>();
    private final Set<Class<?>> missingItemSubPredicateTooltips = new HashSet<>();
    private final Set<Class<?>> missingEntitySubPredicateTooltips = new HashSet<>();
    private final Set<Class<?>> missingDataComponentTypeTooltips = new HashSet<>();
    private final Set<Class<?>> missingConsumeEffectTooltips = new HashSet<>();
    private final Set<Class<?>> missingItemListingFactories = new HashSet<>();

    private final ICommonUtils utils;

    private ServerLevel serverLevel;
    private LootContext lootContext;

    public AliServerRegistry(ICommonUtils utils) {
        this.utils = utils;
    }

    public void clearData() {
        entryItemCollectorMap.clear();
        functionItemCollectorMap.clear();
        numberConverterMap.clear();
        entryFactoryMap.clear();

        functionTooltipMap.clear();
        conditionTooltipMap.clear();
        ingredientTooltipMap.clear();
        itemSubPredicateTooltipMap.clear();
        entitySubPredicateTooltipMap.clear();
        dataComponentTypeTooltipMap.clear();
        consumeEffectTooltipMap.clear();

        chanceModifierMap.clear();
        countModifierMap.clear();
        itemStackModifierMap.clear();

        lootTableMap.clear();
        lootModifierGetters.clear();
        lootModifierMap.clear();

        itemListingFactoryMap.clear();
        tradeItemCollectorMap.clear();

        missingEntryFactories.clear();
        missingFunctionTooltips.clear();
        missingConditionTooltips.clear();
        missingIngredientTooltips.clear();
        missingItemSubPredicateTooltips.clear();
        missingEntitySubPredicateTooltips.clear();
        missingDataComponentTypeTooltips.clear();
        missingConsumeEffectTooltips.clear();
        missingItemListingFactories.clear();
    }

    public void addLootTable(ResourceLocation resourceLocation, LootTable lootTable) {
        lootTableMap.put(resourceLocation, lootTable);
    }

    public void prepareLootModifiers() {
        for (Function<IServerUtils, List<ILootModifier<?>>> lootModifierGetter : lootModifierGetters) {
            lootModifierMap.addAll(lootModifierGetter.apply(this));
        }
    }

    public List<ILootModifier<?>> getLootModifiers() {
        return lootModifierMap;
    }

    public void setServerLevel(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
        this.lootContext = new LootContext(new LootParams(serverLevel, null, Map.of(), 0F), RandomSource.create(), null); //FIXME
    }

    @Override
    public <T extends LootPoolEntryContainer> void registerItemCollector(Class<?> type, BiFunction<IServerUtils, T, List<Item>> itemSupplier) {
        //noinspection unchecked
        entryItemCollectorMap.put(type, (u, e) -> itemSupplier.apply(u, (T) e));
    }

    @Override
    public <T extends LootItemFunction> void registerItemCollector(Class<T> type, TriFunction<IServerUtils, List<Item>, T, List<Item>> itemSupplier) {
        //noinspection unchecked
        functionItemCollectorMap.put(type, (u, l, f) -> itemSupplier.apply(u, l, (T) f));
    }

    @Override
    public <T extends LootPoolEntryContainer> void registerEntry(Class<T> type, EntryFactory<T> entryFactory) {
        entryFactoryMap.put(type, entryFactory);
    }

    @Override
    public <T extends LootItemFunction> void registerFunctionTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        functionTooltipMap.put(type, (u, f) -> getter.apply(u, (T) f));
    }

    @Override
    public <T extends LootItemCondition> void registerConditionTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        conditionTooltipMap.put(type, (u, c) -> getter.apply(u, (T) c));
    }

    @Override
    public <T extends Ingredient> void registerIngredientTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        ingredientTooltipMap.put(type, (u, c) -> getter.apply(u, (T) c));
    }

    @Override
    public <T extends ItemSubPredicate> void registerItemSubPredicateTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        itemSubPredicateTooltipMap.put(type, (u, c) -> getter.apply(u, (T) c));
    }

    @Override
    public <T extends EntitySubPredicate> void registerEntitySubPredicateTooltip(MapCodec<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        entitySubPredicateTooltipMap.put(type, (u, c) -> getter.apply(u, (T) c));
    }

    @Override
    public <T> void registerDataComponentTypeTooltip(DataComponentType<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        dataComponentTypeTooltipMap.put(type, (u, c) -> getter.apply(u, (T) c));
    }

    @Override
    public <T extends ConsumeEffect> void registerConsumeEffectTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        consumeEffectTooltipMap.put(type, (u, c) -> getter.apply(u, (T) c));
    }

    @Override
    public <T extends NumberProvider> void registerNumberProvider(Class<T> type, BiFunction<IServerUtils, T, RangeValue> converter) {
        //noinspection unchecked
        numberConverterMap.put(type, (u, t) -> converter.apply(u, (T) t));
    }

    @Override
    public <T extends LootItemFunction> void registerCountModifier(Class<T> type, TriConsumer<IServerUtils, T, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> consumer) {
        //noinspection unchecked
        countModifierMap.put(type, (u, f, v) -> consumer.accept(u, (T) f, v));
    }

    @Override
    public <T extends LootItemCondition> void registerChanceModifier(Class<T> type, TriConsumer<IServerUtils, T, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> consumer) {
        //noinspection unchecked
        chanceModifierMap.put(type, (u, f, v) -> consumer.accept(u, (T) f, v));
    }

    @Override
    public <T extends LootItemFunction> void registerItemStackModifier(Class<T> type, TriFunction<IServerUtils, T, ItemStack, ItemStack> consumer) {
        //noinspection unchecked
        itemStackModifierMap.put(type, (u, f, i) -> consumer.apply(u, (T) f, i));
    }

    @Override
    public void registerLootModifiers(Function<IServerUtils, List<ILootModifier<?>>> getter) {
        lootModifierGetters.add(getter);
    }

    @Override
    public <T extends VillagerTrades.ItemListing> void registerItemListing(Class<T> type, TriFunction<IServerUtils, T, List<ITooltipNode>, IDataNode> tradeFactory) {
        //noinspection unchecked
        itemListingFactoryMap.put(type, (u, i, c) -> tradeFactory.apply(u, (T) i, c));
    }

    @Override
    public <T extends VillagerTrades.ItemListing> void registerItemListingCollector(Class<T> type, BiFunction<IServerUtils, T, Pair<List<Item>, List<Item>>> itemSupplier) {
        //noinspection unchecked
        tradeItemCollectorMap.put(type, (u, i) -> itemSupplier.apply(u, (T) i));
    }

    @Override
    public <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry) {
        BiFunction<IServerUtils, LootPoolEntryContainer, List<Item>> itemSupplier = entryItemCollectorMap.get(entry.getClass());

        if (itemSupplier != null) {
            return itemSupplier.apply(utils, entry);
        } else {
            return List.of();
        }
    }

    @Override
    public <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function) {
        TriFunction<IServerUtils, List<Item>, LootItemFunction, List<Item>> itemSupplier = functionItemCollectorMap.get(function.getClass());

        if (itemSupplier != null) {
            return itemSupplier.apply(utils, items, function);
        } else {
            return List.of();
        }
    }

    @Override
    public <T extends LootPoolEntryContainer> EntryFactory<T> getEntryFactory(IServerUtils utils, T type) {
        //noinspection unchecked
        EntryFactory<T> entryFactory = (EntryFactory<T>) entryFactoryMap.get(type.getClass());

        if (entryFactory != null) {
            return entryFactory;
        } else {
            missingEntryFactories.add(type.getClass());
            return (utils1, entry, chance, sumWeight, functions, conditions) ->  new MissingNode();
        }
    }

    @Override
    public <T extends LootItemFunction> ITooltipNode getFunctionTooltip(IServerUtils utils, T function) {
        BiFunction<IServerUtils, LootItemFunction, ITooltipNode> entryTooltipGetter = functionTooltipMap.get(function.getClass());

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, function);
        } else {
            missingFunctionTooltips.add(function.getClass());

            try {
                return GenericTooltipUtils.getMissingFunction(utils, function);
            } catch (Throwable e) {
                LOGGER.warn("Function type {} was not registered", function.getClass().getCanonicalName());
                return GenericTooltipUtils.getStringTooltip(utils, "ali.util.advanced_loot_info.missing", function.getClass().getSimpleName());
            }
        }
    }

    @Override
    public <T extends LootItemCondition> ITooltipNode getConditionTooltip(IServerUtils utils, T condition) {
        BiFunction<IServerUtils, LootItemCondition, ITooltipNode> entryTooltipGetter = conditionTooltipMap.get(condition.getClass());

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, condition);
        } else {
            missingConditionTooltips.add(condition.getClass());

            try {
                return GenericTooltipUtils.getMissingCondition(utils, condition);
            } catch (Throwable e) {
                LOGGER.warn("Condition type for {} was not registered", condition.getClass().getCanonicalName());
                return GenericTooltipUtils.getStringTooltip(utils, "ali.util.advanced_loot_info.missing", condition.getClass().getSimpleName());
            }
        }
    }

    @Override
    public <T extends Ingredient> ITooltipNode getIngredientTooltip(IServerUtils utils, T ingredient) {
        BiFunction<IServerUtils, Ingredient, ITooltipNode> ingredientTooltipGetter = ingredientTooltipMap.get(ingredient.getClass());

        if (ingredientTooltipGetter != null) {
            return ingredientTooltipGetter.apply(utils, ingredient);
        } else {
            missingIngredientTooltips.add(ingredient.getClass());
            return GenericTooltipUtils.getStringTooltip(utils, "ali.util.advanced_loot_info.missing", ingredient.getClass().getSimpleName());
        }
    }

    @Override
    public <T extends ItemSubPredicate> ITooltipNode getItemSubPredicateTooltip(IServerUtils utils, T predicate) {
        BiFunction<IServerUtils, ItemSubPredicate, ITooltipNode> itemSubPredicateTooltipGetter = itemSubPredicateTooltipMap.get(predicate.getClass());

        if (itemSubPredicateTooltipGetter != null) {
            return itemSubPredicateTooltipGetter.apply(utils, predicate);
        } else {
            missingItemSubPredicateTooltips.add(predicate.getClass());
            return GenericTooltipUtils.getStringTooltip(utils, "ali.util.advanced_loot_info.missing", predicate.getClass().getSimpleName());
        }
    }

    @Override
    public <T extends EntitySubPredicate> ITooltipNode getEntitySubPredicateTooltip(IServerUtils utils, T predicate) {
        BiFunction<IServerUtils, EntitySubPredicate, ITooltipNode> entitySubPredicateTooltipGetter = entitySubPredicateTooltipMap.get(predicate.codec());

        if (entitySubPredicateTooltipGetter != null) {
            return entitySubPredicateTooltipGetter.apply(utils, predicate);
        } else {
            missingEntitySubPredicateTooltips.add(predicate.getClass());
            return GenericTooltipUtils.getStringTooltip(utils, "ali.util.advanced_loot_info.missing", predicate.getClass().getSimpleName());
        }
    }

    @Override
    public ITooltipNode getDataComponentTypeTooltip(IServerUtils utils, DataComponentType<?> type, Object value) {
        BiFunction<IServerUtils, Object, ITooltipNode> dataComponentTypeTooltipGetter = dataComponentTypeTooltipMap.get(type);

        if (dataComponentTypeTooltipGetter != null) {
            return dataComponentTypeTooltipGetter.apply(utils, value);
        } else {
            missingDataComponentTypeTooltips.add(type.getClass());
            return GenericTooltipUtils.getStringTooltip(utils, "ali.util.advanced_loot_info.missing", type.getClass().getSimpleName());
        }
    }

    @Override
    public <T extends ConsumeEffect> ITooltipNode getConsumeEffectTooltip(IServerUtils utils, T effect) {
        BiFunction<IServerUtils, ConsumeEffect, ITooltipNode> consumeEffectTooltipGetter = consumeEffectTooltipMap.get(effect.getClass());

        if (consumeEffectTooltipGetter != null) {
            return consumeEffectTooltipGetter.apply(utils, effect);
        } else {
            missingConsumeEffectTooltips.add(effect.getClass());
            return GenericTooltipUtils.getStringTooltip(utils, "ali.util.advanced_loot_info.missing", effect.getClass().getSimpleName());
        }
    }

    @Override
    public <T extends LootItemFunction> void applyCountModifier(IServerUtils utils, T function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        TriConsumer<IServerUtils, LootItemFunction, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCountConsumer = countModifierMap.get(function.getClass());

        if (bonusCountConsumer != null) {
            bonusCountConsumer.accept(utils, function, count);
        }
    }

    @Override
    public <T extends LootItemCondition> void applyChanceModifier(IServerUtils utils, T condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        TriConsumer<IServerUtils, LootItemCondition, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChanceConsumer = chanceModifierMap.get(condition.getClass());

        if (bonusChanceConsumer != null) {
            bonusChanceConsumer.accept(utils, condition, chance);
        }
    }

    @Override
    public <T extends LootItemFunction> ItemStack applyItemStackModifier(IServerUtils utils, T function, ItemStack itemStack) {
        TriFunction<IServerUtils, LootItemFunction, ItemStack, ItemStack> bonusChanceConsumer = itemStackModifierMap.get(function.getClass());

        if (bonusChanceConsumer != null) {
            itemStack = bonusChanceConsumer.apply(utils, function, itemStack);
        }

        return itemStack;
    }

    @Override
    public <T extends VillagerTrades.ItemListing> IDataNode getItemListing(IServerUtils utils, T entry, List<ITooltipNode> conditions) {
        //noinspection unchecked
        TriFunction<IServerUtils, T, List<ITooltipNode>, IDataNode> itemListingFactory = (TriFunction<IServerUtils, T, List<ITooltipNode>, IDataNode>) itemListingFactoryMap.get(entry.getClass());

        if (itemListingFactory != null) {
            return itemListingFactory.apply(utils, entry, conditions);
        } else {
            missingItemListingFactories.add(entry.getClass());
            return new MissingNode();
        }
    }

    @Override
    public <T extends VillagerTrades.ItemListing> Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, T entry) {
        //noinspection unchecked
        BiFunction<IServerUtils, T, Pair<List<Item>, List<Item>>> itemCollector = (BiFunction<IServerUtils, T, Pair<List<Item>, List<Item>>>) tradeItemCollectorMap.get(entry.getClass());

        if (itemCollector != null) {
            return itemCollector.apply(utils, entry);
        }

        return new Pair<>(Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public RangeValue convertNumber(IServerUtils utils, @Nullable NumberProvider numberProvider) {
        if (numberProvider != null) {
            BiFunction<IServerUtils, NumberProvider, RangeValue> function = numberConverterMap.get(numberProvider.getClass());

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
    public LootTable getLootTable(Either<ResourceLocation, LootTable> either) {
        return either.map(lootTableMap::get, lootTable -> lootTable);
    }

    @Nullable
    @Override
    public HolderLookup.Provider lookupProvider() {
        return serverLevel != null ? serverLevel.registryAccess() : null;
    }

    public IDataNode parseTable(List<ILootModifier<?>> modifiers, LootTable lootTable) {
        return new LootTableNode(modifiers, this, lootTable);
    }

    public IDataNode parseTrade(Int2ObjectMap<VillagerTrades.ItemListing[]> itemListingMap) {
        return new TradeNode(this, itemListingMap);
    }

    @Override
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        return utils.createEntities(type, level);
    }

    public void printRegistrationInfo() {
        LOGGER.info("Registered {} entry item collectors", entryItemCollectorMap.size());
        LOGGER.info("Registered {} function item collectors", functionItemCollectorMap.size());
        LOGGER.info("Registered {} number converters", numberConverterMap.size());
        LOGGER.info("Registered {} entry factories", entryFactoryMap.size());
        LOGGER.info("Registered {} function tooltips", functionTooltipMap.size());
        LOGGER.info("Registered {} condition tooltips", conditionTooltipMap.size());
        LOGGER.info("Registered {} ingredient tooltips", ingredientTooltipMap.size());
        LOGGER.info("Registered {} item sub predicate tooltips", itemSubPredicateTooltipMap.size());
        LOGGER.info("Registered {} entity sub predicate tooltips", entitySubPredicateTooltipMap.size());
        LOGGER.info("Registered {} data component type tooltips", dataComponentTypeTooltipMap.size());
        LOGGER.info("Registered {} chance modifiers", chanceModifierMap.size());
        LOGGER.info("Registered {} count modifiers", countModifierMap.size());
        LOGGER.info("Registered {} item stack modifiers", itemStackModifierMap.size());
        LOGGER.info("Registered {} loot modifiers", lootModifierMap.size());
        LOGGER.info("Registered {} item listing factories", itemListingFactoryMap.size());
    }

    public void printRuntimeInfo() {
        missingEntryFactories.forEach((t) -> LOGGER.warn("Missing entry factory for {}", t.getName()));
        missingFunctionTooltips.forEach((t) -> LOGGER.warn("Missing function tooltip for {}", t.getCanonicalName()));
        missingConditionTooltips.forEach((t) -> LOGGER.warn("Missing condition tooltip for {}", t.getCanonicalName()));
        missingIngredientTooltips.forEach((t) -> LOGGER.warn("Missing ingredient tooltip for {}", t.getCanonicalName()));
        missingItemSubPredicateTooltips.forEach((t) -> LOGGER.warn("Missing item sub predicate tooltip for {}", t.getCanonicalName()));
        missingEntitySubPredicateTooltips.forEach((t) -> LOGGER.warn("Missing entity sub predicate tooltip for {}", t.getCanonicalName()));
        missingDataComponentTypeTooltips.forEach((t) -> LOGGER.warn("Missing data component type tooltip for {}", t.getCanonicalName()));
        missingConsumeEffectTooltips.forEach((t) -> LOGGER.warn("Missing consume effect tooltip for {}", t.getCanonicalName()));
        missingItemListingFactories.forEach((t) -> LOGGER.warn("Missing trade item listing for {}", t.getName()));
        LOGGER.info("Prepared {} loot tables", lootTableMap.size());
    }
}
