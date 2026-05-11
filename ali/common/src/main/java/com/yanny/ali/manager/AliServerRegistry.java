package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.manager.ClassKeyedMap;
import com.yanny.aci.manager.CoreServerRegistry;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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
import net.minecraft.world.level.storage.loot.*;
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

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AliServerRegistry extends CoreServerRegistry<AliConfig, AliCommonRegistry, IServerUtils> implements IServerRegistry, IServerUtils, ICommonUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    // factories
    private final ManagedRegistry<Class<?>, EntryFactory<?>> entryFactories = registerClassKeyed("entry factories", true, HashMap::new, BuiltInRegistries.LOOT_POOL_ENTRY_TYPE);
    // converters
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, NumberProvider, RangeValue>> numberConverters = registerClassKeyed("number converters", true, HashMap::new, BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE);
    // listings
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, VillagerTrades.ItemListing, TooltipNode, IDataNode>> tradeItemListings = registerClassKeyed("trade item listings", true, HashMap::new, null);
    // collectors
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootPoolEntryContainer, List<Item>>> entryItemCollectors = registerClassKeyed("entry item collectors", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, List<Item>, LootItemFunction, List<Item>>> functionItemCollectors = registerClassKeyed("function item collectors", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, VillagerTrades.ItemListing, Pair<List<Item>, List<Item>>>> tradeItemCollectors = registerClassKeyed("trade item collectors", false, HashMap::new, null);
    // tooltips
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemFunction, TooltipBuilder>> functionTooltips = registerClassKeyed("function tooltips", true, HashMap::new, BuiltInRegistries.LOOT_FUNCTION_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemCondition, TooltipBuilder>> conditionTooltips = registerClassKeyed("condition tooltips", true, HashMap::new, BuiltInRegistries.LOOT_CONDITION_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Ingredient, TooltipBuilder>> ingredientTooltips = registerClassKeyed("ingredient tooltips", true, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Object, TooltipBuilder>> valueTooltips = registerClassKeyed("value tooltips", true, ClassKeyedMap::new, null);
    // modifiers
    private final ManagedRegistry<Class<?>, TriConsumer<IServerUtils, LootItemCondition, Map<Enchantment, Map<Integer, RangeValue>>>> chanceModifiers = registerClassKeyed("chance modifiers", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, TriConsumer<IServerUtils, LootItemFunction, Map<Enchantment, Map<Integer, RangeValue>>>> countModifiers = registerClassKeyed("count modifiers", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, LootItemFunction, ItemStack, ItemStack>> itemStackModifiers = registerClassKeyed("item stack modifiers", false, HashMap::new, null);

    private final Map<ResourceLocation, LootTable> lootTableMap = new HashMap<>();
    private final Map<ResourceLocation, Integer> hitMap = new HashMap<>();
    private final List<Function<IServerUtils, List<ILootModifier<?>>>> lootModifierGetters = new LinkedList<>();
    private final List<ILootModifier<?>> lootModifierMap = new LinkedList<>();

    private LootContext lootContext;

    public AliServerRegistry(AliCommonRegistry utils) {
        super(utils);
    }

    public void clearData() {
        super.clearData();
        lootTableMap.clear();
        lootModifierGetters.clear();
        lootModifierMap.clear();
    }

    public void addLootTable(ResourceLocation resourceLocation, LootTable lootTable) {
        lootTableMap.put(resourceLocation, lootTable);
    }

    public void clearLootTables() {
        lootTableMap.clear();
    }

    public List<ILootModifier<?>> getLootModifiers() {
        return lootModifierMap;
    }

    public void setServerLevel(ServerLevel serverLevel) {
        super.setServerLevel(serverLevel);
        this.lootContext = new LootContext(new LootParams(serverLevel, Map.of(), Map.of(), 0F), RandomSource.create(), new LootDataResolver() {
            @Override
            public @Nullable <T> T getElement(LootDataId<T> lootDataId) {
                return null;
            }
        });
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
    public <T extends LootItemFunction> void registerFunctionTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        functionTooltips.put(type, (u, f) -> getter.apply(u, type.cast(f)));
    }

    @Override
    public <T extends LootItemCondition> void registerConditionTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        conditionTooltips.put(type, (u, c) -> getter.apply(u, type.cast(c)));
    }

    @Override
    public <T extends Ingredient> void registerIngredientTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        ingredientTooltips.put(type, (u, i) -> getter.apply(u, type.cast(i)));
    }

    @Override
    public <T> void registerValueTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        valueTooltips.put(type, (u, v) -> getter.apply(u, type.cast(v)));
    }

    @Override
    public <T extends NumberProvider> void registerNumberProvider(Class<T> type, BiFunction<IServerUtils, T, RangeValue> converter) {
        numberConverters.put(type, (u, n) -> converter.apply(u, type.cast(n)));
    }

    @Override
    public <T extends LootItemFunction> void registerCountModifier(Class<T> type, TriConsumer<IServerUtils, T, Map<Enchantment, Map<Integer, RangeValue>>> consumer) {
        countModifiers.put(type, (u, f, v) -> consumer.accept(u, type.cast(f), v));
    }

    @Override
    public <T extends LootItemCondition> void registerChanceModifier(Class<T> type, TriConsumer<IServerUtils, T, Map<Enchantment, Map<Integer, RangeValue>>> consumer) {
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
    public <T extends VillagerTrades.ItemListing> void registerItemListing(Class<T> type, TriFunction<IServerUtils, T, TooltipNode, IDataNode> tradeFactory) {
        tradeItemListings.put(type, (u, i, c) -> tradeFactory.apply(u, type.cast(i), c));
    }

    @Override
    public <T extends VillagerTrades.ItemListing> void registerItemListingCollector(Class<T> type, BiFunction<IServerUtils, T, Pair<List<Item>, List<Item>>> itemSupplier) {
        tradeItemCollectors.put(type, (u, i) -> itemSupplier.apply(u, (type.cast(i))));
    }

    @NotNull
    @Override
    public <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry) {
        return entryItemCollectors.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(List::of);
    }

    @NotNull
    @Override
    public <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function) {
        return functionItemCollectors.get(function.getClass())
                .map((e) -> e.apply(utils, items, function))
                .orElseGet(List::of);
    }

    @NotNull
    @Override
    public <T extends LootPoolEntryContainer> EntryFactory<T> getEntryFactory(IServerUtils utils, T type) {
        //noinspection unchecked
        return (EntryFactory<T>) entryFactories.get(type.getClass())
                .orElseGet(() -> (u, e, c, s, f, o) -> new MissingNode(MissingTooltipUtils.getMissingEntryTooltip(u, e).build()));
    }

    @NotNull
    @Override
    public <T extends LootItemFunction> TooltipBuilder getFunctionTooltip(IServerUtils utils, T function) {
        return functionTooltips.get(function.getClass())
                .map((f) -> f.apply(utils, function))
                .orElseGet(() -> MissingTooltipUtils.getMissingFunctionTooltip(utils, function));
    }

    @NotNull
    @Override
    public <T extends LootItemCondition> TooltipBuilder getConditionTooltip(IServerUtils utils, T condition) {
        return conditionTooltips.get(condition.getClass())
                .map((c) -> c.apply(utils, condition))
                .orElseGet(() -> MissingTooltipUtils.getMissingConditionTooltip(utils, condition));
    }

    @NotNull
    @Override
    public <T extends Ingredient> TooltipBuilder getIngredientTooltip(IServerUtils utils, T ingredient) {
        return ingredientTooltips.get(ingredient.getClass())
                .map((i) -> i.apply(utils, ingredient))
                .orElseGet(() -> MissingTooltipUtils.getMissingIngredientTooltip(utils, ingredient));
    }

    @NotNull
    @Override
    public <T> TooltipBuilder getValueTooltip(IServerUtils utils, @Nullable T value) {
        if (value == null) {
            return TooltipBuilder.empty();
        }

        Class<?> valueClass = value.getClass();

        if (valueClass.isArray()) {
            return TooltipBuilder.branch((b) -> {
                for (int i = 0; i < Array.getLength(value); i++) {
                    b.add(utils.getValueTooltip(utils, Array.get(value, i)));
                }
            });
        } else {
            return valueTooltips.get(valueClass)
                    .map((v) -> v.apply(utils, value))
                    .orElseGet(() -> MissingTooltipUtils.getMissingValueTooltip(utils, value));
        }
    }

    @Override
    public <T extends LootItemFunction> void applyCountModifier(IServerUtils utils, T function, Map<Enchantment, Map<Integer, RangeValue>> count) {
        countModifiers.get(function.getClass()).ifPresent((m) -> m.accept(utils, function, count));
    }

    @Override
    public <T extends LootItemCondition> void applyChanceModifier(IServerUtils utils, T condition, Map<Enchantment, Map<Integer, RangeValue>> chance) {
        chanceModifiers.get(condition.getClass()).ifPresent((m) -> m.accept(utils, condition, chance));
    }

    @NotNull
    @Override
    public <T extends LootItemFunction> ItemStack applyItemStackModifier(IServerUtils utils, T function, final ItemStack itemStack) {
        return itemStackModifiers.get(function.getClass())
                .map((m) -> m.apply(utils, function, itemStack))
                .orElse(itemStack);
    }

    @NotNull
    @Override
    public <T extends VillagerTrades.ItemListing> IDataNode getItemListing(IServerUtils utils, T entry, TooltipNode condition) {
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
                        return new MissingNode(MissingTooltipUtils.getMissingItemListingTooltip(utils, entry).build());
                    } catch (Throwable e) {
                        return new MissingNode(TooltipNode.EMPTY_INSTANCE);
                    }
                });
    }

    @NotNull
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

    @NotNull
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
    public LootContext getLootContext() {
        return lootContext;
    }

    @Nullable
    @Override
    public LootTable getLootTable(ResourceLocation resourceLocation) {
        hitMap.compute(resourceLocation, (k, v) -> v == null ? 1 : v + 1);
        return lootTableMap.get(resourceLocation);
    }

    @NotNull
    @Override
    public List<LootPool> getLootPools(LootTable lootTable) {
        return Services.getPlatform().getLootPools(lootTable);
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

    @NotNull
    @Override
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        return commonUtils.createEntities(type, level);
    }

    public void printRegistrationInfo() {
        super.printRegistrationInfo();
        prepareLootModifiers();
        LOGGER.info("Registered {} loot modifiers", lootModifierMap.size());
    }

    @Override
    public void printRuntimeInfo() {
        super.printRuntimeInfo();

        if (this.getConfiguration().logMoreStatistics) {
            TooltipNode.logCacheStatistics();
        }
    }

    private void prepareLootModifiers() {
        for (Function<IServerUtils, List<ILootModifier<?>>> lootModifierGetter : lootModifierGetters) {
            lootModifierMap.addAll(lootModifierGetter.apply(this));
        }
    }
}
