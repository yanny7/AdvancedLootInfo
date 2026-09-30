package com.yanny.ali.manager;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.manager.ClassKeyedMap;
import com.yanny.aci.manager.CoreServerRegistry;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.aci.manager.NumberConverters;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.glm.*;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class AliServerRegistry extends CoreServerRegistry<AliConfig, AliCommonRegistry, IServerUtils> implements IServerRegistry, IServerUtils, ICommonUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    // factories
    private final ManagedRegistry<Class<?>, EntryFactory<?>> entryFactories = registerClassKeyed("entry factories", true, HashMap::new, BuiltInRegistries.LOOT_POOL_ENTRY_TYPE);
    // converters
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, NumberProvider, NumberExpr>> numberConverters = registerClassKeyed("number converters", true, HashMap::new, BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, NumberProvider, NumberExpr>> intNumberConverters = registerClassKeyed("int number converters", false, HashMap::new, null);
    // listings
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, VillagerTrades.ItemListing, TooltipNode, IDataNode>> tradeItemListings = registerClassKeyed("trade item listings", true, HashMap::new, null);
    // traders
    private final ManagedRegistry<ResourceLocation, Trades> trades = register("trades", false, HashMap::new, ResourceLocation::toString, null);
    // tooltips
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootPoolEntryContainer, TooltipBuilder>> entryTooltips = registerClassKeyed("entry tooltips", true, HashMap::new, BuiltInRegistries.LOOT_POOL_ENTRY_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemFunction, TooltipBuilder>> functionTooltips = registerClassKeyed("function tooltips", true, HashMap::new, BuiltInRegistries.LOOT_FUNCTION_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemCondition, TooltipBuilder>> conditionTooltips = registerClassKeyed("condition tooltips", true, HashMap::new, BuiltInRegistries.LOOT_CONDITION_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Ingredient, TooltipBuilder>> ingredientTooltips = registerClassKeyed("ingredient tooltips", true, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Object, TooltipBuilder>> valueTooltips = registerClassKeyed("value tooltips", true, ClassKeyedMap::new, null);
    // modifiers
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, LootItemCondition, NumberExpr, NumberExpr>> chanceModifiers = registerClassKeyed("chance modifiers", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, LootItemFunction, NumberExpr, NumberExpr>> countModifiers = registerClassKeyed("count modifiers", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, LootItemFunction, ItemStack, ItemStack>> itemStackModifiers = registerClassKeyed("item stack modifiers", false, HashMap::new, null);
    // unwrappers
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemFunction, List<LootItemFunction>>> functionUnwrappers = registerClassKeyed("function unwrappers", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemCondition, List<LootItemCondition>>> conditionUnwrappers = registerClassKeyed("condition unwrappers", false, HashMap::new, null);
    // global loot modifier pages
    private final ManagedRegistry<Class<?>, IPageResolver<Object>> pageResolvers = registerClassKeyed("global loot modifier page resolvers", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, IEntitySubPredicateResolver<EntitySubPredicate>> entitySubPredicateResolvers = registerClassKeyed("entity sub-predicate resolvers", false, HashMap::new, null);
    // translations
    private final ManagedRegistry<Class<?>, EnumTranslation> enumValues = registerClassKeyed("enum values", true, HashMap::new, null);

    private final Set<String> fallbackItemListings = new HashSet<>();
    private final Map<ResourceLocation, LootTable> lootTableMap = new HashMap<>();
    private final Map<ResourceLocation, Integer> hitMap = new HashMap<>();
    private final List<Function<IServerUtils, List<ILootModifier<?>>>> lootModifierGetters = new LinkedList<>();
    private final List<Function<Ingredient, Object>> ingredientUnwrappers = new LinkedList<>();
    private final List<ILootModifier<?>> lootModifierMap = new LinkedList<>();
    private final List<Function<IServerUtils, List<IPageLootModifier>>> pageLootModifierGetters = new LinkedList<>();
    private final List<IPageLootModifier> pageLootModifiers = new LinkedList<>();
    private final List<ILootContextPreparer> lootContextPreparers = new ArrayList<>();

    private final LootContext lootContext;

    public AliServerRegistry(AliCommonRegistry utils, ServerLevel level) {
        super(utils, level);
        this.lootContext = new LootContext(new LootParams(level, Map.of(), Map.of(), 0F), RandomSource.create(), new LootDataResolver() {
            @Override
            public @Nullable <T> T getElement(LootDataId<T> lootDataId) {
                return null;
            }
        });
    }

    public void clearData() {
        super.clearData();
        fallbackItemListings.clear();
        lootTableMap.clear();
        ingredientUnwrappers.clear();
        lootModifierGetters.clear();
        lootModifierMap.clear();
        pageLootModifierGetters.clear();
        pageLootModifiers.clear();
        lootContextPreparers.clear();
    }

    public void addLootTable(ResourceLocation resourceLocation, LootTable lootTable) {
        lootTableMap.put(resourceLocation, lootTable);
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        lootTableMap.clear();
    }

    public List<ILootModifier<?>> getLootModifiers() {
        return lootModifierMap;
    }

    public List<IPageLootModifier> getPageLootModifiers() {
        return pageLootModifiers;
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
    public <T extends NumberProvider> void registerNumberProvider(Class<T> type, BiFunction<IServerUtils, T, NumberExpr> converter) {
        numberConverters.put(type, (u, n) -> converter.apply(u, type.cast(n)));
    }

    @Override
    public <T extends NumberProvider> void registerNumberProvider(Class<T> type, BiFunction<IServerUtils, T, NumberExpr> converter, BiFunction<IServerUtils, T, NumberExpr> intConverter) {
        registerNumberProvider(type, converter);
        intNumberConverters.put(type, (u, n) -> intConverter.apply(u, type.cast(n)));
    }

    @Override
    public <T extends LootItemFunction> void registerCountModifier(Class<T> type, TriFunction<IServerUtils, T, NumberExpr, NumberExpr> modifier) {
        countModifiers.put(type, (u, f, v) -> modifier.apply(u, type.cast(f), v));
    }

    @Override
    public <T extends LootItemCondition> void registerChanceModifier(Class<T> type, TriFunction<IServerUtils, T, NumberExpr, NumberExpr> modifier) {
        chanceModifiers.put(type, (u, c, v) -> modifier.apply(u, type.cast(c), v));
    }

    @Override
    public <T extends LootItemFunction> void registerItemStackModifier(Class<T> type, TriFunction<IServerUtils, T, ItemStack, ItemStack> consumer) {
        itemStackModifiers.put(type, (u, f, i) -> consumer.apply(u, type.cast(f), i));
    }

    @Override
    public <T extends LootItemFunction> void registerFunctionUnwrapper(Class<T> type, BiFunction<IServerUtils, T, List<LootItemFunction>> unwrapper) {
        functionUnwrappers.put(type, (u, f) -> unwrapper.apply(u, type.cast(f)));
    }

    @Override
    public <T extends LootItemCondition> void registerConditionUnwrapper(Class<T> type, BiFunction<IServerUtils, T, List<LootItemCondition>> unwrapper) {
        conditionUnwrappers.put(type, (u, c) -> unwrapper.apply(u, type.cast(c)));
    }

    @Override
    public void registerIngredientUnwrapper(Function<Ingredient, Object> unwrapper) {
        ingredientUnwrappers.add(unwrapper);
    }

    @Override
    public <T> void registerPageResolver(Class<T> type, IPageResolver<T> resolver) {
        pageResolvers.put(type, (u, v, p) -> resolver.test(u, type.cast(v), p));
    }

    @Override
    public <T extends EntitySubPredicate> void registerEntitySubPredicateResolver(Class<T> type, IEntitySubPredicateResolver<T> resolver) {
        entitySubPredicateResolvers.put(type, (u, s, p) -> resolver.test(u, type.cast(s), p));
    }

    @Override
    public void registerLootContextPreparer(ILootContextPreparer preparer) {
        lootContextPreparers.add(preparer);
    }

    @Override
    public void registerLootModifiers(Function<IServerUtils, List<ILootModifier<?>>> getter) {
        lootModifierGetters.add(getter);
    }

    @Override
    public void registerGlobalLootModifiers(Function<IServerUtils, List<IPageLootModifier>> getter) {
        pageLootModifierGetters.add(getter);
    }

    @Override
    public <T extends VillagerTrades.ItemListing> void registerItemListing(Class<T> type, TriFunction<IServerUtils, T, TooltipNode, IDataNode> tradeFactory) {
        tradeItemListings.put(type, (u, i, c) -> tradeFactory.apply(u, type.cast(i), c));
    }

    @Override
    public void registerTrades(ResourceLocation traderId, @Nullable EntityType<?> entityType, Supplier<Int2ObjectMap<VillagerTrades.ItemListing[]>> itemListings, IntFunction<TradeLevelInfo> levelInfo) {
        trades.put(traderId, new Trades(entityType, itemListings, levelInfo));
    }

    public Map<ResourceLocation, Trades> getTrades() {
        return trades.entries();
    }

    @Override
    public void registerEnumTranslation(Class<? extends Enum<?>> type, String modId, String owner) {
        enumValues.put(type, new EnumTranslation(modId, owner));
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
                return valueTooltips.get(unwrapped.getClass())
                        .map((v) -> v.apply(utils, unwrapped))
                        .orElseGet(() -> MissingTooltipUtils.getMissingIngredientTooltip(utils, ingredient));
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

    @Override
    @NotNull
    public <T extends LootItemFunction> NumberExpr applyCountModifier(IServerUtils utils, T function, NumberExpr count, List<TooltipNode> conditions) {
        NumberExpr result = count;

        for (LootItemFunction f : unwrapFunction(utils, function)) {
            Optional<TriFunction<IServerUtils, LootItemFunction, NumberExpr, NumberExpr>> modifier = countModifiers.get(f.getClass());

            if (modifier.isPresent()) {
                try {
                    NumberExpr modified = modifier.get().apply(utils, f, result);

                    if (f instanceof LootItemConditionalFunction conditional && conditional.predicates.length > 0) {
                        result = TooltipUtils.conditional(utils, result, modified, Arrays.asList(conditional.predicates), conditions);
                    } else {
                        result = modified;
                    }
                } catch (Throwable e) {
                    String id = String.valueOf(BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(f.getType()));

                    LOGGER.warn("Failed to apply count modifier {}: {}", id, e.getMessage(), e);
                    result = NumberExpr.opaque(id);
                }
            }
        }

        return result;
    }

    @Override
    @NotNull
    public <T extends LootItemCondition> NumberExpr applyChanceModifier(IServerUtils utils, T condition, NumberExpr chance) {
        NumberExpr result = chance;

        for (LootItemCondition c : unwrapCondition(utils, condition)) {
            Optional<TriFunction<IServerUtils, LootItemCondition, NumberExpr, NumberExpr>> modifier = chanceModifiers.get(c.getClass());

            if (modifier.isPresent()) {
                try {
                    result = modifier.get().apply(utils, c, result);
                } catch (Throwable e) {
                    String id = String.valueOf(BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(c.getType()));

                    LOGGER.warn("Failed to apply chance modifier {}: {}", id, e.getMessage(), e);
                    result = NumberExpr.opaque(id);
                }
            }
        }

        return result;
    }

    @NotNull
    @Override
    public <T extends LootItemFunction> ItemStack applyItemStackModifier(IServerUtils utils, T function, final ItemStack itemStack) {
        ItemStack result = itemStack;

        for (LootItemFunction f : unwrapFunction(utils, function)) {
            ItemStack stack = result;
            result = itemStackModifiers.get(f.getClass())
                    .map((m) -> m.apply(utils, f, stack))
                    .orElse(stack);
        }

        return result;
    }

    @NotNull
    @Override
    public List<LootItemFunction> unwrapFunction(IServerUtils utils, LootItemFunction function) {
        List<LootItemFunction> result = new ArrayList<>();

        unwrap(utils, function, functionUnwrappers, Collections.newSetFromMap(new IdentityHashMap<>()), result);
        return result;
    }

    @NotNull
    @Override
    public List<LootItemCondition> unwrapCondition(IServerUtils utils, LootItemCondition condition) {
        List<LootItemCondition> result = new ArrayList<>();

        unwrap(utils, condition, conditionUnwrappers, Collections.newSetFromMap(new IdentityHashMap<>()), result);
        return result;
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
                            String name = ManagedRegistry.classKeyName(entry.getClass());

                            if (fallbackItemListings.add(name)) {
                                LOGGER.info("Using MerchantOffer fallback for trade item listing {}, reported values can be inaccurate", name);
                            }

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

        return TooltipBuilder.component(Component.translatableWithFallback(key, value.name()));
    }

    @NotNull
    @Override
    public NumberExpr convertNumber(IServerUtils utils, NumberProvider numberProvider) {
        return NumberConverters.convert(getModId(), numberConverters, utils, numberProvider, AliServerRegistry::numberProviderTypeId);
    }

    @NotNull
    @Override
    public NumberExpr convertIntNumber(IServerUtils utils, NumberProvider numberProvider) {
        if (intNumberConverters.get(numberProvider.getClass()).isPresent()) {
            return NumberConverters.convert(getModId(), intNumberConverters, utils, numberProvider, AliServerRegistry::numberProviderTypeId);
        }

        return NumberExpr.fn(NumberFunctions.ROUND, convertNumber(utils, numberProvider));
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

    @NotNull
    @Override
    public Verdict testPage(IServerUtils utils, Object value, LootPage page) {
        LootDataResolver lootData = utils.getServerLevel().getServer().getLootData();

        return GlobalLootModifierUtils.testPage(utils, value, page, pageResolvers.get(value.getClass()).orElse(null), lootContextPreparers, lootData);
    }

    @NotNull
    @Override
    public ParamState getParamState(LootPage page, LootContextParam<?> param) {
        return GlobalLootModifierUtils.getParamState(page, param);
    }

    @Nullable
    @Override
    public Verdict testEntitySubPredicate(IServerUtils utils, EntitySubPredicate predicate, LootPage page) {
        return entitySubPredicateResolvers.get(predicate.getClass())
                .map((r) -> r.test(utils, predicate, page))
                .orElse(null);
    }

    public IDataNode parseTable(List<IOperation> operations, LootTable lootTable) {
        return NodeUtils.getLootTableNode(operations, this, lootTable, 1, Collections.emptyList(), Collections.emptyList());
    }

    public IDataNode parseTable(List<IOperation> operations) {
        return NodeUtils.getLootTableNode(operations);
    }

    public IDataNode parseTrade(Trades trades) {
        return new TradeNode(this, trades.entityType(), trades.itemListings().get(), trades.levelInfo());
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
        LOGGER.info("Registered {} loot modifiers", lootModifierMap.size() + pageLootModifiers.size());
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

            for (Function<IServerUtils, List<IPageLootModifier>> pageLootModifierGetter : pageLootModifierGetters) {
                pageLootModifiers.addAll(pageLootModifierGetter.apply(this));
            }
        } finally {
            TooltipContext.clearPalette();
        }
    }

    @NotNull
    private static String numberProviderTypeId(NumberProvider numberProvider) {
        return String.valueOf(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE.getKey(numberProvider.getType()));
    }

    public record Trades(@Nullable EntityType<?> entityType, Supplier<Int2ObjectMap<VillagerTrades.ItemListing[]>> itemListings, IntFunction<TradeLevelInfo> levelInfo) {}

    private static <T> void unwrap(IServerUtils utils, T value, ManagedRegistry<Class<?>, BiFunction<IServerUtils, T, List<T>>> unwrappers, Set<Object> visiting, List<T> result) {
        // predicate and item modifier references can form cycles, vanilla only logs them
        if (!visiting.add(value)) {
            result.add(value);
            return;
        }

        List<T> inner = unwrappers.get(value.getClass()).map((u) -> u.apply(utils, value)).orElse(null);

        if (inner != null) {
            inner.forEach((i) -> unwrap(utils, i, unwrappers, visiting, result));
        } else {
            result.add(value);
        }

        visiting.remove(value);
    }
}
