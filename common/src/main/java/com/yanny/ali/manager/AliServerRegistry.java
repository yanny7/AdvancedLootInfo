package com.yanny.ali.manager;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.yanny.ali.api.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ErrorTooltipNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.MerchantOffer;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class AliServerRegistry implements IServerRegistry, IServerUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<ManagedRegistry<?, ?>> allRegistries = new ArrayList<>();
    // factories
    private final ManagedRegistry<Class<?>, EntryFactory<?>> entryFactories = registerClassKeyed("entry factories", true, HashMap::new);
    // converters
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, NumberProvider, RangeValue>> numberConverters = registerClassKeyed("number converters", true, HashMap::new);
    // listings
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, VillagerTrades.ItemListing, ITooltipNode, IDataNode>> tradeItemListings = registerClassKeyed("trade item listings", true, HashMap::new);
    // collectors
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootPoolEntryContainer, List<Item>>> entryItemCollectors = registerClassKeyed("entry item collectors", false, HashMap::new);
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, List<Item>, LootItemFunction, List<Item>>> functionItemCollectors = registerClassKeyed("function item collectors", false, HashMap::new);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, VillagerTrades.ItemListing, Pair<List<Item>, List<Item>>>> tradeItemCollectors = registerClassKeyed("trade item collectors", false, HashMap::new);
    // tooltips
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemFunction, ITooltipNode>> functionTooltips = registerClassKeyed("function tooltips", true, HashMap::new);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemCondition, ITooltipNode>> conditionTooltips = registerClassKeyed("condition tooltips", true, HashMap::new);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Ingredient, ITooltipNode>> ingredientTooltips = registerClassKeyed("ingredient tooltips", true, HashMap::new);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Object, IKeyTooltipNode>> valueTooltips = registerClassKeyed("value tooltips", true, ClassKeyedMap::new);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, ItemSubPredicate, ITooltipNode>> itemSubPredicateTooltips = registerClassKeyed("item sub predicate tooltips", true, HashMap::new);;
    private final ManagedRegistry<MapCodec<?>, BiFunction<IServerUtils, EntitySubPredicate, ITooltipNode>> entitySubPredicateTooltips = register("entity sub predicate tooltips", true, HashMap::new, AliServerRegistry::mapCodecNameGetter);
    private final ManagedRegistry<DataComponentType<?>, BiFunction<IServerUtils, Object, ITooltipNode>> dataComponentTypeTooltips = register("data component type tooltips", true, HashMap::new, AliServerRegistry::dataComponentTypeNameGetter);
    // modifiers
    private final ManagedRegistry<Class<?>, TriConsumer<IServerUtils, LootItemCondition, Map<Holder<Enchantment>, Map<Integer, RangeValue>>>> chanceModifiers = registerClassKeyed("chance modifiers", false, HashMap::new);
    private final ManagedRegistry<Class<?>, TriConsumer<IServerUtils, LootItemFunction, Map<Holder<Enchantment>, Map<Integer, RangeValue>>>> countModifiers = registerClassKeyed("count modifiers", false, HashMap::new);
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, LootItemFunction, ItemStack, ItemStack>> itemStackModifiers = registerClassKeyed("item stack modifiers", false, HashMap::new);

    private final Map<ResourceLocation, LootTable> lootTableMap = new HashMap<>();
    private final Map<ResourceLocation, Integer> hitMap = new HashMap<>();
    private final List<Function<IServerUtils, List<ILootModifier<?>>>> lootModifierGetters = new LinkedList<>();
    private final List<ILootModifier<?>> lootModifierMap = new LinkedList<>();

    private final ICommonUtils utils;

    private ServerLevel serverLevel;
    private LootContext lootContext;
    private ResourceLocation currentLootTable;

    public AliServerRegistry(ICommonUtils utils) {
        this.utils = utils;
    }

    public void clearData() {
        lootTableMap.clear();
        lootModifierGetters.clear();
        lootModifierMap.clear();

        allRegistries.forEach(ManagedRegistry::clear);
    }

    public void addLootTable(ResourceLocation resourceLocation, LootTable lootTable) {
        lootTableMap.put(resourceLocation, lootTable);
    }

    public void setCurrentLootTable(@Nullable ResourceLocation location) {
        currentLootTable = location;
    }

    public void clearLootTables() {
        lootTableMap.clear();
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
        this.lootContext = new LootContext(new LootParams(serverLevel, Map.of(), Map.of(), 0F), RandomSource.create(), null); //FIXME
    }

    @Override
    public <T extends LootPoolEntryContainer> void registerItemCollector(Class<T> type, BiFunction<IServerUtils, T, List<Item>> itemSupplier) {
        entryItemCollectors.put(type, (u, e) -> itemSupplier.apply(u, type.cast(e)));
    }

    @Override
    public <T extends LootItemFunction> void registerItemCollector(Class<T> type, TriFunction<IServerUtils, List<Item>, T, List<Item>> itemSupplier) {
        functionItemCollectors.put(type, (u, l, f) -> itemSupplier.apply(u, l, type.cast(f)));
    }

    @Override
    public <T extends LootPoolEntryContainer> void registerEntry(Class<T> type, EntryFactory<T> entry) {
        entryFactories.put(type, entry);
    }

    @Override
    public <T extends LootItemFunction> void registerFunctionTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        functionTooltips.put(type, (u, f) -> getter.apply(u, type.cast(f)));
    }

    @Override
    public <T extends LootItemCondition> void registerConditionTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        conditionTooltips.put(type, (u, c) -> getter.apply(u, type.cast(c)));
    }

    @Override
    public <T extends Ingredient> void registerIngredientTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        ingredientTooltips.put(type, (u, i) -> getter.apply(u, type.cast(i)));
    }

    @Override
    public <T> void registerValueTooltip(Class<T> type, BiFunction<IServerUtils, T, IKeyTooltipNode> getter) {
        valueTooltips.put(type, (u, v) -> getter.apply(u, type.cast(v)));
    }

    @Override
    public <T extends ItemSubPredicate> void registerItemSubPredicateTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        itemSubPredicateTooltips.put(type, (u, i) -> getter.apply(u, type.cast(i)));
    }

    @Override
    public <T extends EntitySubPredicate> void registerEntitySubPredicateTooltip(MapCodec<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        entitySubPredicateTooltips.put(type, (u, c) -> getter.apply(u, (T) c));
    }

    @Override
    public <T> void registerDataComponentTypeTooltip(DataComponentType<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter) {
        //noinspection unchecked
        dataComponentTypeTooltips.put(type, (u, c) -> getter.apply(u, (T) c));
    }

    @Override
    public <T extends NumberProvider> void registerNumberProvider(Class<T> type, BiFunction<IServerUtils, T, RangeValue> converter) {
        numberConverters.put(type, (u, n) -> converter.apply(u, type.cast(n)));
    }

    @Override
    public <T extends LootItemFunction> void registerCountModifier(Class<T> type, TriConsumer<IServerUtils, T, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> consumer) {
        countModifiers.put(type, (u, f, v) -> consumer.accept(u, type.cast(f), v));
    }

    @Override
    public <T extends LootItemCondition> void registerChanceModifier(Class<T> type, TriConsumer<IServerUtils, T, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> consumer) {
        chanceModifiers.put(type, (u, c, v) -> consumer.accept(u, type.cast(c), v));
    }

    @Override
    public <T extends LootItemFunction> void registerItemStackModifier(Class<T> type, TriFunction<IServerUtils, T, ItemStack, ItemStack> consumer) {
        itemStackModifiers.put(type, (u, f, i) -> consumer.apply(u, type.cast(f), i));
    }

    @Override
    public void registerLootModifiers(Function<IServerUtils, List<ILootModifier<?>>> getter) {
        lootModifierGetters.add(getter);
    }

    @Override
    public <T extends VillagerTrades.ItemListing> void registerItemListing(Class<T> type, TriFunction<IServerUtils, T, ITooltipNode, IDataNode> tradeFactory) {
        tradeItemListings.put(type, (u, i, c) -> tradeFactory.apply(u, type.cast(i), c));
    }

    @Override
    public <T extends VillagerTrades.ItemListing> void registerItemListingCollector(Class<T> type, BiFunction<IServerUtils, T, Pair<List<Item>, List<Item>>> itemSupplier) {
        tradeItemCollectors.put(type, (u, i) -> itemSupplier.apply(u, (type.cast(i))));
    }

    @Override
    public <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry) {
        return entryItemCollectors.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(List::of);
    }

    @Override
    public <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function) {
        return functionItemCollectors.get(function.getClass())
                .map((e) -> e.apply(utils, items, function))
                .orElseGet(List::of);
    }

    @Override
    public <T extends LootPoolEntryContainer> EntryFactory<T> getEntryFactory(IServerUtils utils, T type) {
        //noinspection unchecked
        return (EntryFactory<T>) entryFactories.get(type.getClass())
                .orElseGet(() -> (u, e, c, s, f, o) -> new MissingNode(GenericTooltipUtils.getMissingEntryTooltip(u, e)));
    }

    @Override
    public <T extends LootItemFunction> ITooltipNode getFunctionTooltip(IServerUtils utils, T function) {
        return functionTooltips.get(function.getClass())
                .map((f) -> f.apply(utils, function))
                .orElseGet(() -> GenericTooltipUtils.getMissingFunctionTooltip(utils, function));
    }

    @Override
    public <T extends LootItemCondition> ITooltipNode getConditionTooltip(IServerUtils utils, T condition) {
        return conditionTooltips.get(condition.getClass())
                .map((c) -> c.apply(utils, condition))
                .orElseGet(() -> GenericTooltipUtils.getMissingConditionTooltip(utils, condition));
    }

    @Override
    public <T extends Ingredient> ITooltipNode getIngredientTooltip(IServerUtils utils, T ingredient) {
        return ingredientTooltips.get(ingredient.getClass())
                .map((i) -> i.apply(utils, ingredient))
                .orElseGet(() -> GenericTooltipUtils.getMissingIngredientTooltip(utils, ingredient));
    }

    @Override
    public <T> IKeyTooltipNode getValueTooltip(IServerUtils utils, @Nullable T value) {
        if (value == null) {
            return EmptyTooltipNode.empty();
        }

        Class<?> valueClass = value.getClass();

        if (valueClass.isArray()) {
            return TooltipUtils.getArrayTooltip(utils, value);
        } else {
            return valueTooltips.get(valueClass)
                    .map((v) -> v.apply(utils, value))
                    .orElseGet(() -> ErrorTooltipNode.error("[" + valueClass.getName() + "]"));
        }
    }

    @Override
    public <T extends ItemSubPredicate> ITooltipNode getItemSubPredicateTooltip(IServerUtils utils, T predicate) {
        return itemSubPredicateTooltips.get(predicate.getClass())
                .map((i) -> i.apply(utils, predicate))
                .orElseGet(() -> utils.getValueTooltip(utils, predicate.getClass().getSimpleName()).build("ali.util.advanced_loot_info.missing"));
    }

    @Override
    public <T extends EntitySubPredicate> ITooltipNode getEntitySubPredicateTooltip(IServerUtils utils, T predicate) {
        return entitySubPredicateTooltips.get(predicate.codec())
                .map((i) -> i.apply(utils, predicate))
                .orElseGet(() -> utils.getValueTooltip(utils, predicate.getClass().getSimpleName()).build("ali.util.advanced_loot_info.missing"));
    }

    @Override
    public ITooltipNode getDataComponentTypeTooltip(IServerUtils utils, DataComponentType<?> type, Object value) {
        return dataComponentTypeTooltips.get(type)
                .map((i) -> i.apply(utils, value))
                .orElseGet(() -> utils.getValueTooltip(utils, type.getClass().getSimpleName()).build("ali.util.advanced_loot_info.missing"));
    }

    @Override
    public <T extends LootItemFunction> void applyCountModifier(IServerUtils utils, T function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        countModifiers.get(function.getClass()).ifPresent((m) -> m.accept(utils, function, count));
    }

    @Override
    public <T extends LootItemCondition> void applyChanceModifier(IServerUtils utils, T condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        chanceModifiers.get(condition.getClass()).ifPresent((m) -> m.accept(utils, condition, chance));
    }

    @Override
    public <T extends LootItemFunction> ItemStack applyItemStackModifier(IServerUtils utils, T function, final ItemStack itemStack) {
        return itemStackModifiers.get(function.getClass())
                .map((m) -> m.apply(utils, function, itemStack))
                .orElse(itemStack);
    }

    @Override
    public <T extends VillagerTrades.ItemListing> IDataNode getItemListing(IServerUtils utils, T entry, ITooltipNode condition) {
        return tradeItemListings.get(entry.getClass())
                .map((e) -> e.apply(utils, entry, condition))
                .orElseGet(() -> {
                    try {
                        // try to get result from MerchantOffer. only if params aren't used (otherwise values can be dynamic)
                        //noinspection DataFlowIssue
                        MerchantOffer offer = entry.getOffer(null, null);

                        if (offer != null) {
                            return TradeUtils.getNode(utils, offer, condition);
                        }
                    } catch (Throwable ignored) {}

                    try {
                        return new MissingNode(GenericTooltipUtils.getMissingItemListingTooltip(utils, entry));
                    } catch (Throwable e) {
                        return new MissingNode(EmptyTooltipNode.EMPTY);
                    }
                });
    }

    @Override
    public <T extends VillagerTrades.ItemListing> Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, T entry) {
        return tradeItemCollectors.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> {
                    try {
                        // try to get result from MerchantOffer. only if params aren't used (otherwise values can be dynamic)
                        //noinspection DataFlowIssue
                        MerchantOffer offer = entry.getOffer(null, null);

                        if (offer != null) {
                            return TradeUtils.collectItems(utils, offer);
                        }
                    } catch (Throwable ignored) {}

                    return new Pair<>(Collections.emptyList(), Collections.emptyList());
                });
    }

    @Override
    public RangeValue convertNumber(IServerUtils utils, @Nullable NumberProvider numberProvider) {
        if (numberProvider != null) {
            return numberConverters.get(numberProvider.getClass())
                    .map((c) -> c.apply(utils, numberProvider))
                    .orElseGet(() -> new RangeValue(false, true));
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
    public ResourceLocation getCurrentLootTable() {
        return currentLootTable;
    }

    @Nullable
    @Override
    public LootTable getLootTable(Either<ResourceLocation, LootTable> either) {
        either.ifLeft((resourceLocation) -> hitMap.compute(resourceLocation, (k, v) -> v == null ? 1 : v + 1));
        either.ifRight((lootTable) -> {
            Optional<Map.Entry<ResourceLocation, LootTable>> entry = lootTableMap.entrySet().stream().filter((l) -> l.getValue().equals(lootTable)).findFirst();

            entry.ifPresent(e -> hitMap.compute(e.getKey(), (k, v) -> v == null ? 1 : v + 1));
        });
        return either.map(lootTableMap::get, lootTable -> lootTable);
    }

    @Nullable
    @Override
    public HolderLookup.Provider lookupProvider() {
        return serverLevel != null ? serverLevel.registryAccess() : null;
    }

    public IDataNode parseTable(List<ILootModifier<?>> modifiers, LootTable lootTable) {
        return NodeUtils.getLootTableNode(modifiers, this, lootTable, 1, Collections.emptyList(), Collections.emptyList());
    }

    public IDataNode parseTable(List<ILootModifier<?>> modifiers) {
        return NodeUtils.getLootTableNode(modifiers);
    }

    public IDataNode parseTrade(Int2ObjectMap<VillagerTrades.ItemListing[]> itemListingMap) {
        return new TradeNode(this, itemListingMap);
    }

    public boolean isSubTable(ResourceLocation resourceLocation) {
        Integer hitCount = hitMap.get(resourceLocation);

        return hitCount != null && lootTableMap.getOrDefault(resourceLocation, LootTable.EMPTY).getParamSet() == LootTable.DEFAULT_PARAM_SET;
    }

    @Override
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        return utils.createEntities(type, level);
    }

    @Override
    public AliConfig getConfiguration() {
        return utils.getConfiguration();
    }

    public void printRegistrationInfo() {
        allRegistries.forEach(ManagedRegistry::logStatistics);

        LOGGER.info("Registered {} loot modifiers", lootModifierMap.size());
    }

    public void printRuntimeInfo() {
        allRegistries.forEach(ManagedRegistry::logMissing);
    }

    @NotNull
    private <V> ManagedRegistry<Class<?>, V> registerClassKeyed(String label, boolean reportMissing, Supplier<Map<Class<?>, V>> mapSupplier) {
        return register(label, reportMissing, mapSupplier, Class::getTypeName);
    }

    @NotNull
    private <K, V> ManagedRegistry<K, V> register(String label, boolean reportMissing, Supplier<Map<K, V>> mapSupplier, Function<K, String> keyNameGetter) {
        ManagedRegistry<K, V> reg = new ManagedRegistry<>(label, reportMissing, mapSupplier, keyNameGetter);
        allRegistries.add(reg);
        return reg;
    }

    private static String mapCodecNameGetter(MapCodec<?> codec) {
        //noinspection unchecked
        ResourceLocation key = BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE.getKey((MapCodec<? extends EntitySubPredicate>) codec);

        if (key != null) {
            return key.toString();
        } else {
            return codec.getClass().getTypeName();
        }
    }

    private static String dataComponentTypeNameGetter(DataComponentType<?> dataComponentType) {
        ResourceLocation key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(dataComponentType);

        if (key != null) {
            return key.toString();
        } else {
            return dataComponentType.getClass().getTypeName();
        }
    }
}
