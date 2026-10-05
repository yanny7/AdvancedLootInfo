package com.yanny.ali.manager;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberConverter;
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
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.glm.*;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
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
    private final ManagedRegistry<Class<?>, NumberConverter<IServerUtils, LootPoolEntryContainer>> entryWeights = registerClassKeyed("entry weights", true, ClassKeyedMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootPoolEntryContainer, List<LootPoolEntryContainer>>> entryChildren = registerClassKeyed("entry children", false, ClassKeyedMap::new, null);
    private final ManagedRegistry<Class<?>, NumberConverter<IServerUtils, NumberProvider>> numberConverters = registerClassKeyed("number converters", true, HashMap::new, BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE);
    private final ManagedRegistry<Class<?>, NumberConverter<IServerUtils, NumberProvider>> intNumberConverters = registerClassKeyed("int number converters", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, LevelBasedValue, NumberExpr, NumberExpr>> levelBasedValueConverters = registerClassKeyed("level based value converters", true, HashMap::new, BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE);
    // listings
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, VillagerTrades.ItemListing, TooltipNode, IDataNode>> tradeItemListings = registerClassKeyed("trade item listings", true, HashMap::new, null);
    // traders
    private final ManagedRegistry<Identifier, Trades> trades = register("trades", false, HashMap::new, Identifier::toString, null);
    // tooltips
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootPoolEntryContainer, TooltipBuilder>> entryTooltips = registerClassKeyed("entry tooltips", true, HashMap::new, BuiltInRegistries.LOOT_POOL_ENTRY_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemFunction, TooltipBuilder>> functionTooltips = registerClassKeyed("function tooltips", true, HashMap::new, BuiltInRegistries.LOOT_FUNCTION_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemCondition, TooltipBuilder>> conditionTooltips = registerClassKeyed("condition tooltips", true, HashMap::new, BuiltInRegistries.LOOT_CONDITION_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Ingredient, TooltipBuilder>> ingredientTooltips = registerClassKeyed("ingredient tooltips", true, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, Object, TooltipBuilder>> valueTooltips = registerClassKeyed("value tooltips", true, ClassKeyedMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, DataComponentPredicate, TooltipBuilder>> dataComponentPredicateTooltips = registerClassKeyed("data component predicate tooltips", true, HashMap::new, BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE);
    private final ManagedRegistry<MapCodec<?>, BiFunction<IServerUtils, EntitySubPredicate, TooltipBuilder>> entitySubPredicateTooltips = register("entity sub predicate tooltips", true, HashMap::new, AliServerRegistry::mapCodecNameGetter, BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE);
    private final ManagedRegistry<DataComponentType<?>, BiFunction<IServerUtils, Object, TooltipBuilder>> dataComponentTypeTooltips = register("data component type tooltips", true, HashMap::new, AliServerRegistry::dataComponentTypeNameGetter, BuiltInRegistries.DATA_COMPONENT_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, ConsumeEffect, TooltipBuilder>> consumeEffectTooltips = registerClassKeyed("consume effect tooltips", true, HashMap::new, BuiltInRegistries.CONSUME_EFFECT_TYPE);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, SlotSource, TooltipBuilder>> slotSourceTooltips = registerClassKeyed("slot source tooltips", true, HashMap::new, BuiltInRegistries.SLOT_SOURCE_TYPE);
    // modifiers
    private final ManagedRegistry<Class<?>, NumberModifier<LootItemCondition>> chanceModifiers = registerClassKeyed("chance modifiers", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, NumberModifier<LootItemFunction>> countModifiers = registerClassKeyed("count modifiers", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, TriFunction<IServerUtils, LootItemFunction, ItemStack, ItemStack>> itemStackModifiers = registerClassKeyed("item stack modifiers", false, HashMap::new, null);
    // unwrappers
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemFunction, List<LootItemFunction>>> functionUnwrappers = registerClassKeyed("function unwrappers", false, HashMap::new, null);
    private final ManagedRegistry<Class<?>, BiFunction<IServerUtils, LootItemCondition, List<LootItemCondition>>> conditionUnwrappers = registerClassKeyed("condition unwrappers", false, HashMap::new, null);
    // global loot modifier pages
    private final ManagedRegistry<Class<?>, IPageResolver<Object>> pageResolvers = registerClassKeyed("global loot modifier page resolvers", false, HashMap::new, null);
    private final ManagedRegistry<MapCodec<?>, IEntitySubPredicateResolver<EntitySubPredicate>> entitySubPredicateResolvers = register("entity sub-predicate resolvers", false, HashMap::new, AliServerRegistry::mapCodecNameGetter, null);
    // translations
    private final ManagedRegistry<Class<?>, EnumTranslation> enumValues = registerClassKeyed("enum values", true, HashMap::new, null);

    private final Set<String> fallbackItemListings = new HashSet<>();
    private final Set<String> failedRenderers = new HashSet<>();
    private final Map<Identifier, LootTable> lootTableMap = new HashMap<>();
    private final Map<Identifier, Integer> hitMap = new HashMap<>();
    private final List<Function<IServerUtils, List<ILootModifier<?>>>> lootModifierGetters = new LinkedList<>();
    private final List<Function<Ingredient, Object>> ingredientUnwrappers = new LinkedList<>();
    private final List<ILootModifier<?>> lootModifierMap = new LinkedList<>();
    private final List<Function<IServerUtils, List<IPageLootModifier>>> pageLootModifierGetters = new LinkedList<>();
    private final List<IPageLootModifier> pageLootModifiers = new LinkedList<>();
    private final List<ILootContextPreparer> lootContextPreparers = new ArrayList<>();

    private final LootContext lootContext;

    public AliServerRegistry(AliCommonRegistry utils, ServerLevel level) {
        super(utils, level);
        this.lootContext = new LootContext(new LootParams(level, null, Map.of(), 0F), RandomSource.create(), null); //FIXME
    }

    public void clearData() {
        super.clearData();
        fallbackItemListings.clear();
        failedRenderers.clear();
        NodeUtils.clearFailedOperations();
        lootTableMap.clear();
        ingredientUnwrappers.clear();
        lootModifierGetters.clear();
        lootModifierMap.clear();
        pageLootModifierGetters.clear();
        pageLootModifiers.clear();
        lootContextPreparers.clear();
    }

    public void addLootTable(Identifier Identifier, LootTable lootTable) {
        lootTableMap.put(Identifier, lootTable);
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
    public <T extends LootPoolEntryContainer> void registerEntryWeight(Class<T> type, NumberConverter<IServerUtils, T> weight) {
        entryWeights.put(type, (u, e, c) -> weight.convert(u, type.cast(e), c));
    }

    @Override
    public <T extends LootPoolEntryContainer> void registerEntryChildren(Class<T> type, BiFunction<IServerUtils, T, List<LootPoolEntryContainer>> children) {
        entryChildren.put(type, (u, e) -> children.apply(u, type.cast(e)));
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
    public <T extends DataComponentPredicate> void registerDataComponentPredicateTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        dataComponentPredicateTooltips.put(type, (u, i) -> getter.apply(u, type.cast(i)));
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
    public <T extends ConsumeEffect> void registerConsumeEffectTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        consumeEffectTooltips.put(type, (u, c) -> getter.apply(u, type.cast(c)));
    }

    @Override
    public <T extends SlotSource> void registerSlotSourceTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter) {
        slotSourceTooltips.put(type, (u, s) -> getter.apply(u, type.cast(s)));
    }

    @Override
    public <T extends NumberProvider> void registerNumberProvider(Class<T> type, NumberConverter<IServerUtils, T> converter) {
        numberConverters.put(type, (u, n, c) -> converter.convert(u, type.cast(n), c));
    }

    @Override
    public <T extends NumberProvider> void registerNumberProvider(Class<T> type, NumberConverter<IServerUtils, T> converter, NumberConverter<IServerUtils, T> intConverter) {
        registerNumberProvider(type, converter);
        intNumberConverters.put(type, (u, n, c) -> intConverter.convert(u, type.cast(n), c));
    }

    @Override
    public <T extends LevelBasedValue> void registerLevelBasedValue(Class<T> type, TriFunction<IServerUtils, T, NumberExpr, NumberExpr> converter) {
        levelBasedValueConverters.put(type, (u, v, l) -> converter.apply(u, type.cast(v), l));
    }

    @Override
    public <T extends LootItemFunction> void registerCountModifier(Class<T> type, NumberModifier<T> modifier) {
        countModifiers.put(type, (u, f, v, c) -> modifier.apply(u, type.cast(f), v, c));
    }

    @Override
    public <T extends LootItemCondition> void registerChanceModifier(Class<T> type, NumberModifier<T> modifier) {
        chanceModifiers.put(type, (u, c, v, l) -> modifier.apply(u, type.cast(c), v, l));
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
    public <T extends EntitySubPredicate> void registerEntitySubPredicateResolver(MapCodec<T> type, IEntitySubPredicateResolver<T> resolver) {
        //noinspection unchecked
        entitySubPredicateResolvers.put(type, (u, s, p) -> resolver.test(u, (T) s, p));
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
    public void registerTrades(Identifier traderId, @Nullable EntityType<?> entityType, Supplier<Int2ObjectMap<VillagerTrades.ItemListing[]>> itemListings, IntFunction<TradeLevelInfo> levelInfo) {
        trades.put(traderId, new Trades(entityType, itemListings, levelInfo));
    }

    public Map<Identifier, Trades> getTrades() {
        return trades.entries();
    }

    @Override
    public void registerEnumTranslation(Class<? extends Enum<?>> type, String modId, String owner) {
        enumValues.put(type, new EnumTranslation(modId, owner));
    }

    @NotNull
    @Override
    public <T extends LootPoolEntryContainer> EntryFactory<T> getEntryFactory(IServerUtils utils, T type) {
        EntryFactory<T> missing = (u, e, c, s, l, f, o) -> new MissingNode(MissingTooltipUtils.getMissingEntryTooltip(u, e).build());

        //noinspection unchecked
        return entryFactories.get(type.getClass())
                .<EntryFactory<T>>map((factory) -> (u, e, c, s, l, f, o) -> guarded("entry", e, () -> ((EntryFactory<T>) factory).create(u, e, c, s, l, f, o), () -> missing.create(u, e, c, s, l, f, o)))
                .orElse(missing);
    }

    @NotNull
    @Override
    public <T extends LootPoolEntryContainer> NumberExpr getEntryWeight(IServerUtils utils, T entry, List<TooltipNode> conditions) {
        if (entryWeights.get(entry.getClass()).isEmpty()) {
            return NumberExpr.constant(0);
        }

        return NumberConverters.convert(getModId(), entryWeights, utils, entry, conditions, (e) -> String.valueOf(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.getKey(e.getType())));
    }

    @NotNull
    @Override
    public <T extends LootPoolEntryContainer> List<LootPoolEntryContainer> getEntryChildren(IServerUtils utils, T entry) {
        return entryChildren.get(entry.getClass())
                .map((c) -> guarded("entry children", entry, () -> c.apply(utils, entry), List::<LootPoolEntryContainer>of))
                .orElseGet(List::of);
    }

    @NotNull
    @Override
    public <T extends LootPoolEntryContainer> TooltipBuilder getEntryTooltip(IServerUtils utils, T entry) {
        return entryTooltips.get(entry.getClass())
                .map((e) -> guarded("entry tooltip", entry, () -> e.apply(utils, entry), () -> MissingTooltipUtils.getMissingEntryTooltip(utils, entry)))
                .orElseGet(() -> MissingTooltipUtils.getMissingEntryTooltip(utils, entry));
    }

    @NotNull
    @Override
    public <T extends LootItemFunction> TooltipBuilder getFunctionTooltip(IServerUtils utils, T function) {
        return functionTooltips.get(function.getClass())
                .map((f) -> guarded("function tooltip", function, () -> f.apply(utils, function), () -> MissingTooltipUtils.getMissingFunctionTooltip(utils, function)))
                .orElseGet(() -> MissingTooltipUtils.getMissingFunctionTooltip(utils, function));
    }

    @NotNull
    @Override
    public <T extends LootItemCondition> TooltipBuilder getConditionTooltip(IServerUtils utils, T condition) {
        return conditionTooltips.get(condition.getClass())
                .map((c) -> guarded("condition tooltip", condition, () -> c.apply(utils, condition), () -> MissingTooltipUtils.getMissingConditionTooltip(utils, condition)))
                .orElseGet(() -> MissingTooltipUtils.getMissingConditionTooltip(utils, condition));
    }

    @NotNull
    @Override
    public <T extends Ingredient> TooltipBuilder getIngredientTooltip(IServerUtils utils, T ingredient) {
        for (Function<Ingredient, Object> unwrapper : ingredientUnwrappers) {
            Object unwrapped = unwrapper.apply(ingredient);

            if (unwrapped != null) {
                return valueTooltips.get(unwrapped.getClass())
                        .map((v) -> guarded("ingredient tooltip", unwrapped, () -> v.apply(utils, unwrapped), () -> MissingTooltipUtils.getMissingIngredientTooltip(utils, ingredient)))
                        .orElseGet(() -> MissingTooltipUtils.getMissingIngredientTooltip(utils, ingredient));
            }
        }

        return ingredientTooltips.get(ingredient.getClass())
                .map((i) -> guarded("ingredient tooltip", ingredient, () -> i.apply(utils, ingredient), () -> MissingTooltipUtils.getMissingIngredientTooltip(utils, ingredient)))
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
                    .map((v) -> guarded("value tooltip", value, () -> v.apply(utils, value), () -> MissingTooltipUtils.getMissingValueTooltip(utils, value)))
                    .orElseGet(() -> MissingTooltipUtils.getMissingValueTooltip(utils, value));
        }
    }

    @NotNull
    @Override
    public <T extends DataComponentPredicate> TooltipBuilder getDataComponentPredicateTooltip(IServerUtils utils, T predicate) {
        return dataComponentPredicateTooltips.get(predicate.getClass())
                .map((i) -> i.apply(utils, predicate))
                .orElseGet(() -> MissingTooltipUtils.getMissingDataComponentPredicateTooltip(utils, predicate));
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
    public <T extends ConsumeEffect> TooltipBuilder getConsumeEffectTooltip(IServerUtils utils, T effect) {
        return consumeEffectTooltips.get(effect.getClass())
                .map((i) -> guarded("consume effect tooltip", effect, () -> i.apply(utils, effect), () -> MissingTooltipUtils.getMissingConsumableEffectTooltip(utils, effect)))
                .orElseGet(() -> MissingTooltipUtils.getMissingConsumableEffectTooltip(utils, effect));
    }

    @Override
    public <T extends SlotSource> TooltipBuilder getSlotSourceTooltip(IServerUtils utils, T slotSource) {
        return slotSourceTooltips.get(slotSource.getClass())
                .map((i) -> guarded("slot source tooltip", slotSource, () -> i.apply(utils, slotSource), () -> MissingTooltipUtils.getMissingSlotSourceTooltip(utils, slotSource)))
                .orElseGet(() -> MissingTooltipUtils.getMissingSlotSourceTooltip(utils, slotSource));
    }

    @Override
    @NotNull
    public <T extends LootItemFunction> NumberExpr applyCountModifier(IServerUtils utils, T function, NumberExpr count, List<TooltipNode> conditions) {
        NumberExpr result = count;

        for (LootItemFunction f : unwrapFunction(utils, function)) {
            Optional<NumberModifier<LootItemFunction>> modifier = countModifiers.get(f.getClass());

            if (modifier.isPresent()) {
                try {
                    NumberExpr modified = modifier.get().apply(utils, f, result, conditions);

                    if (f instanceof LootItemConditionalFunction conditional && !conditional.predicates.isEmpty()) {
                        result = TooltipUtils.conditional(utils, result, modified, conditional.predicates, conditions);
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
    public <T extends LootItemCondition> NumberExpr applyChanceModifier(IServerUtils utils, T condition, NumberExpr chance, List<TooltipNode> conditions) {
        NumberExpr result = chance;

        for (LootItemCondition c : unwrapCondition(utils, condition)) {
            Optional<NumberModifier<LootItemCondition>> modifier = chanceModifiers.get(c.getClass());

            if (modifier.isPresent()) {
                try {
                    result = modifier.get().apply(utils, c, result, conditions);
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

            try {
                result = itemStackModifiers.get(f.getClass())
                        .map((m) -> m.apply(utils, f, stack))
                        .orElse(stack);
            } catch (Throwable e) {
                LOGGER.warn("Failed to apply item stack modifier {}: {}", BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(f.getType()), e.getMessage(), e);
            }
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
                .map((e) -> guarded("item listing", entry, () -> e.apply(utils, entry, condition), () -> getFallbackItemListing(utils, entry, condition)))
                .orElseGet(() -> getFallbackItemListing(utils, entry, condition));
    }

    @NotNull
    private IDataNode getFallbackItemListing(IServerUtils utils, VillagerTrades.ItemListing entry, TooltipNode condition) {
        try {
            // try to get result from MerchantOffer. only if params aren't used (otherwise values can be dynamic)
            //noinspection DataFlowIssue
            MerchantOffer offer = entry.getOffer(null, null, null);

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
    }

    private <R> R guarded(String kind, Object value, Supplier<R> renderer, Supplier<R> fallback) {
        try {
            return renderer.get();
        } catch (Throwable e) {
            String name = ManagedRegistry.classKeyName(value.getClass());

            if (failedRenderers.add(kind + " " + name)) {
                LOGGER.warn("Failed to build {} for {}, showing it as unsupported: {}", kind, name, e.getMessage(), e);
            }

            return fallback.get();
        }
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
    public NumberExpr convertNumber(IServerUtils utils, NumberProvider numberProvider, List<TooltipNode> conditions) {
        return NumberConverters.convert(getModId(), numberConverters, utils, numberProvider, conditions, AliServerRegistry::numberProviderTypeId);
    }

    @NotNull
    @Override
    public NumberExpr convertIntNumber(IServerUtils utils, NumberProvider numberProvider, List<TooltipNode> conditions) {
        if (intNumberConverters.get(numberProvider.getClass()).isPresent()) {
            return NumberConverters.convert(getModId(), intNumberConverters, utils, numberProvider, conditions, AliServerRegistry::numberProviderTypeId);
        }

        return NumberExpr.fn(NumberFunctions.ROUND, convertNumber(utils, numberProvider, conditions));
    }

    @NotNull
    @Override
    public NumberExpr convertLevelBasedValue(IServerUtils utils, LevelBasedValue value, NumberExpr level) {
        Optional<TriFunction<IServerUtils, LevelBasedValue, NumberExpr, NumberExpr>> converter = levelBasedValueConverters.get(value.getClass());

        if (converter.isEmpty()) {
            return NumberExpr.opaque(levelBasedValueTypeId(value));
        }

        try {
            return converter.get().apply(utils, value, level);
        } catch (Throwable e) {
            String id = levelBasedValueTypeId(value);

            LOGGER.warn("Failed to convert level based value {}: {}", id, e.getMessage(), e);
            return NumberExpr.opaque(id);
        }
    }

    @Nullable
    @Override
    public LootContext getLootContext() {
        return lootContext;
    }

    @Nullable
    @Override
    public LootTable getLootTable(Either<Identifier, LootTable> either) {
        either.ifLeft((Identifier) -> hitMap.compute(Identifier, (k, v) -> v == null ? 1 : v + 1));
        either.ifRight((lootTable) -> {
            Optional<Map.Entry<Identifier, LootTable>> entry = lootTableMap.entrySet().stream().filter((l) -> l.getValue().equals(lootTable)).findFirst();

            entry.ifPresent(e -> hitMap.compute(e.getKey(), (k, v) -> v == null ? 1 : v + 1));
        });
        return either.map(lootTableMap::get, lootTable -> lootTable);
    }

    @NotNull
    @Override
    public Verdict testPage(IServerUtils utils, Object value, LootPage page) {
        HolderGetter.Provider lootData = utils.getServerLevel().getServer().reloadableRegistries().lookup();

        return GlobalLootModifierUtils.testPage(utils, value, page, pageResolvers.get(value.getClass()).orElse(null), lootContextPreparers, lootData);
    }

    @NotNull
    @Override
    public ParamState getParamState(LootPage page, ContextKey<?> param) {
        return GlobalLootModifierUtils.getParamState(page, param);
    }

    @Nullable
    @Override
    public Verdict testEntitySubPredicate(IServerUtils utils, EntitySubPredicate predicate, LootPage page) {
        return entitySubPredicateResolvers.get(predicate.codec())
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
    public boolean isSubTable(Identifier Identifier) {
        Integer hitCount = hitMap.get(Identifier);

        return hitCount != null && lootTableMap.getOrDefault(Identifier, LootTable.EMPTY).getParamSet() == LootTable.DEFAULT_PARAM_SET;
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

    private static String mapCodecNameGetter(MapCodec<?> codec) {
        //noinspection unchecked
        Identifier key = BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE.getKey((MapCodec<? extends EntitySubPredicate>) codec);

        if (key != null) {
            return key.toString();
        } else {
            return codec.getClass().getTypeName();
        }
    }

    private static String dataComponentTypeNameGetter(DataComponentType<?> dataComponentType) {
        Identifier key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(dataComponentType);

        if (key != null) {
            return key.toString();
        } else {
            return dataComponentType.getClass().getTypeName();
        }
    }

    @NotNull
    private static String levelBasedValueTypeId(LevelBasedValue value) {
        return String.valueOf(BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE.getKey(value.codec()));
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

        List<T> inner;

        try {
            inner = unwrappers.get(value.getClass()).map((u) -> u.apply(utils, value)).orElse(null);
        } catch (Throwable e) {
            LOGGER.warn("Failed to unwrap {}: {}", ManagedRegistry.classKeyName(value.getClass()), e.getMessage(), e);
            inner = null;
        }

        if (inner != null) {
            inner.forEach((i) -> unwrap(utils, i, unwrappers, visiting, result));
        } else {
            result.add(value);
        }

        visiting.remove(value);
    }
}
