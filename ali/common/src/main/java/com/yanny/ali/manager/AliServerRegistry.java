package com.yanny.ali.manager;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.manager.ClassKeyedMap;
import com.yanny.aci.manager.CoreServerRegistry;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AliServerRegistry extends CoreServerRegistry<AliConfig, AliCommonRegistry, IServerUtils> implements IServerRegistry, IServerUtils, ICommonUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    // factories
    private final ManagedRegistry<Class<?>, EntryFactory<?>> entryFactories = registerClassKeyed("entry factories", true, HashMap::new, BuiltInRegistries.LOOT_POOL_ENTRY_TYPE);
    // converters
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, NumberProvider, RangeValue>> numberConverters = registerClassKeyed("number converters", true, HashMap::new, BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE);
    // listings
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, VillagerTrades.ItemListing, TooltipNode, IDataNode>> tradeItemListings = registerClassKeyed("trade item listings", true, HashMap::new, null);
    // collectors
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootPoolEntryContainer, List<Item>>> entryItemCollectors = registerClassKeyed("entry item collectors", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, List<Item>, LootItemFunction, List<Item>>> functionItemCollectors = registerClassKeyed("function item collectors", false, HashMap::new, null);
    // tooltips
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootPoolEntryContainer, TooltipBuilder>> entryTooltips = registerClassKeyed("entry tooltips", true, HashMap::new, BuiltInRegistries.LOOT_POOL_ENTRY_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemFunction, TooltipBuilder>> functionTooltips = registerClassKeyed("function tooltips", true, HashMap::new, BuiltInRegistries.LOOT_FUNCTION_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemCondition, TooltipBuilder>> conditionTooltips = registerClassKeyed("condition tooltips", true, HashMap::new, BuiltInRegistries.LOOT_CONDITION_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Ingredient, TooltipBuilder>> ingredientTooltips = registerClassKeyed("ingredient tooltips", true, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Object, TooltipBuilder>> valueTooltips = registerClassKeyed("value tooltips", true, ClassKeyedMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, ItemSubPredicate, TooltipBuilder>> itemSubPredicateTooltips = registerClassKeyed("item sub predicate tooltips", true, HashMap::new, BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE);
    private final ManagedRegistry<MapCodec<?>, BiFunction<IServerUtils, EntitySubPredicate, TooltipBuilder>> entitySubPredicateTooltips = register("entity sub predicate tooltips", true, HashMap::new, AliServerRegistry::mapCodecNameGetter, BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE);
    private final ManagedRegistry<DataComponentType<?>, BiFunction<IServerUtils, Object, TooltipBuilder>> dataComponentTypeTooltips = register("data component type tooltips", true, HashMap::new, AliServerRegistry::dataComponentTypeNameGetter, BuiltInRegistries.DATA_COMPONENT_TYPE);
    // modifiers
    private final ManagedRegistry<Class<?>, TriConsumer<IServerUtils, LootItemCondition, EnchantedRanges>> chanceModifiers = registerClassKeyed("chance modifiers", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, TriConsumer<IServerUtils, LootItemFunction, EnchantedRanges>> countModifiers = registerClassKeyed("count modifiers", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, LootItemFunction, ItemStack, ItemStack>> itemStackModifiers = registerClassKeyed("item stack modifiers", false, HashMap::new, null);
    // translations
    private final ManagedRegistry<Class<?>, EnumTranslation> enumValues = registerClassKeyed("enum values", true, HashMap::new, null);

    private final Map<ResourceLocation, LootTable> lootTableMap = new HashMap<>();
    private final Map<ResourceLocation, Integer> hitMap = new HashMap<>();
    private final List<Function<IServerUtils, List<ILootModifier<?>>>> lootModifierGetters = new LinkedList<>();
    private final List<Function<Ingredient, Object>> ingredientUnwrappers = new LinkedList<>();
    private final List<ILootModifier<?>> lootModifierMap = new LinkedList<>();

    private final LootContext lootContext;

    public AliServerRegistry(AliCommonRegistry utils, ServerLevel level) {
        super(utils, level);
        this.lootContext = new LootContext(new LootParams(level, Map.of(), Map.of(), 0F), RandomSource.create(), null);
    }

    public void clearData() {
        super.clearData();
        lootTableMap.clear();
        ingredientUnwrappers.clear();
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
    public <T extends LootPoolEntryContainer> void registerEntryTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        entryTooltips.put(type, (u, e) -> getter.apply(u, type.cast(e)));
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
    public <T extends ItemSubPredicate> void registerItemSubPredicateTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        itemSubPredicateTooltips.put(type, (u, i) -> getter.apply(u, type.cast(i)));
    }

    @Override
    public <T extends EntitySubPredicate> void registerEntitySubPredicateTooltip(MapCodec<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        entitySubPredicateTooltips.put(type, (u, c) -> getter.apply(u, (T) c));
    }

    @Override
    public <T> void registerDataComponentTypeTooltip(DataComponentType<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        //noinspection unchecked
        dataComponentTypeTooltips.put(type, (u, c) -> getter.apply(u, (T) c));
    }

    @Override
    public <T extends NumberProvider> void registerNumberProvider(Class<T> type, BiFunction<IServerUtils, T, RangeValue> converter) {
        numberConverters.put(type, (u, n) -> converter.apply(u, type.cast(n)));
    }

    @Override
    public <T extends LootItemFunction> void registerCountModifier(Class<T> type, TriConsumer<IServerUtils, T, EnchantedRanges> consumer) {
        countModifiers.put(type, (u, f, v) -> consumer.accept(u, type.cast(f), v));
    }

    @Override
    public <T extends LootItemCondition> void registerChanceModifier(Class<T> type, TriConsumer<IServerUtils, T, EnchantedRanges> consumer) {
        chanceModifiers.put(type, (u, c, v) -> consumer.accept(u, type.cast(c), v));
    }

    @Override
    public <T extends LootItemFunction> void registerItemStackModifier(Class<T> type, TriFunction<IServerUtils, T, ItemStack, ItemStack> consumer) {
        itemStackModifiers.put(type, (u, f, i) -> consumer.apply(u, type.cast(f), i));
    }

    @Override
    public void registerIngredientUnwrapper(Function<Ingredient, Object> unwrapper) {
        ingredientUnwrappers.add(unwrapper);
    }

    @Override
    public void registerLootModifiers(Function<IServerUtils, List<ILootModifier<?>>> getter) {
        lootModifierGetters.add(getter);
    }

    @Override
    public <T extends VillagerTrades.ItemListing> void registerItemListing(Class<T> type, TriFunction<IServerUtils, T, TooltipNode, IDataNode> tradeFactory) {
        tradeItemListings.put(type, (u, i, c) -> tradeFactory.apply(u, type.cast(i), c));
    }

    @Deprecated(forRemoval = true, since = "2.2.0")
    @Override
    public void registerEnumTranslation(Class<? extends Enum<?>> type, String owner) {
        registerEnumTranslation(type, Utils.MOD_ID, owner);
    }

    @Override
    public void registerEnumTranslation(Class<? extends Enum<?>> type, String modId, String owner) {
        enumValues.put(type, new EnumTranslation(modId, owner));
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
    public <T extends LootPoolEntryContainer> TooltipBuilder getEntryTooltip(IServerUtils utils, T entry) {
        return entryTooltips.get(entry.getClass())
                .map((e) -> e.apply(utils, entry))
                .orElseGet(() -> MissingTooltipUtils.getMissingEntryTooltip(utils, entry));
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
        for (Function<Ingredient, Object> unwrapper : ingredientUnwrappers) {
            Object unwrapped = unwrapper.apply(ingredient);

            if (unwrapped != null) {
                return getValueTooltip(utils, unwrapped);
            }
        }

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
                    b.add(TooltipBuilder.asElement(utils.getValueTooltip(utils, Array.get(value, i)), Array.getLength(value)));
                }
            });
        } else {
            return valueTooltips.get(valueClass)
                    .map((v) -> v.apply(utils, value))
                    .orElseGet(() -> MissingTooltipUtils.getMissingValueTooltip(utils, value));
        }
    }

    @NotNull
    @Override
    public <T extends ItemSubPredicate> TooltipBuilder getItemSubPredicateTooltip(IServerUtils utils, T predicate) {
        return itemSubPredicateTooltips.get(predicate.getClass())
                .map((i) -> i.apply(utils, predicate))
                .orElseGet(() -> MissingTooltipUtils.getMissingItemSubPredicateTooltip(utils, predicate));
    }

    @NotNull
    @Override
    public <T extends EntitySubPredicate> TooltipBuilder getEntitySubPredicateTooltip(IServerUtils utils, T predicate) {
        return entitySubPredicateTooltips.get(predicate.codec())
                .map((i) -> i.apply(utils, predicate))
                .orElseGet(() -> MissingTooltipUtils.getMissingEntitySubPredicateTooltip(utils, predicate));
    }

    @NotNull
    @Override
    public TooltipBuilder getDataComponentTypeTooltip(IServerUtils utils, DataComponentType<?> type, Object value) {
        return dataComponentTypeTooltips.get(type)
                .map((i) -> i.apply(utils, value))
                .orElseGet(() -> MissingTooltipUtils.getMissingDataComponentTypeTooltip(utils, type, value));
    }

    @Override
    public <T extends LootItemFunction> void applyCountModifier(IServerUtils utils, T function, EnchantedRanges count) {
        countModifiers.get(function.getClass()).ifPresent((m) -> m.accept(utils, function, count));
    }

    @Override
    public <T extends LootItemCondition> void applyChanceModifier(IServerUtils utils, T condition, EnchantedRanges chance) {
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
                        return new MissingNode(TooltipNode.empty());
                    }
                });
    }

    @NotNull
    @Override
    public TooltipBuilder getEnumTranslation(IServerUtils utils, Enum<?> value) {
        Class<?> type = value.getDeclaringClass();
        EnumTranslation translation = enumValues.get(type).orElseGet(() -> new EnumTranslation(Utils.MOD_ID, CoreTooltipUtils.enumOwnerPath(type)));
        String key = CoreTooltipUtils.enumKey(translation.modId(), translation.owner(), value.name());

        return TooltipBuilder.component(utils.lookupProvider(), Component.translatableWithFallback(key, value.name()));
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
    public LootTable getLootTable(Either<ResourceLocation, LootTable> either) {
        either.ifLeft((resourceLocation) -> hitMap.compute(resourceLocation, (k, v) -> v == null ? 1 : v + 1));
        either.ifRight((lootTable) -> {
            Optional<Map.Entry<ResourceLocation, LootTable>> entry = lootTableMap.entrySet().stream().filter((l) -> l.getValue().equals(lootTable)).findFirst();

            entry.ifPresent(e -> hitMap.compute(e.getKey(), (k, v) -> v == null ? 1 : v + 1));
        });
        return either.map(lootTableMap::get, lootTable -> lootTable);
    }

    public IDataNode parseTable(List<ILootModifier<?>> modifiers, LootTable lootTable) {
        return NodeUtils.getLootTableNode(modifiers, this, lootTable, 1, Collections.emptyList(), Collections.emptyList());
    }

    public IDataNode parseTable(List<ILootModifier<?>> modifiers) {
        return NodeUtils.getLootTableNode(modifiers);
    }

    public IDataNode parseTrade(Int2ObjectMap<VillagerTrades.ItemListing[]> itemListingMap, boolean isWanderingTrader) {
        return new TradeNode(this, itemListingMap, isWanderingTrader);
    }

    // hitCount != null means this table is referenced from another table's tree; the paramSet check
    // limits "sub table" detection to tables with no context type of their own, so tables that are
    // independently meaningful despite being referenced elsewhere still show up under their own category.
    // Known gap: vanilla sub-tables that DO declare a concrete type purely to resolve their own
    // conditions (per-color sheep drop/shearing tables) show up as duplicate top-level entries.
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
            getTooltipCache().logStatistics();
        }
    }

    private void prepareLootModifiers() {
        TooltipContext.setPalette(getTooltipCache());

        try {
            for (Function<IServerUtils, List<ILootModifier<?>>> lootModifierGetter : lootModifierGetters) {
                lootModifierMap.addAll(lootModifierGetter.apply(this));
            }
        } finally {
            TooltipContext.clearPalette();
        }
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
