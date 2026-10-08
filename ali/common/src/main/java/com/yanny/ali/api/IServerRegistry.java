package com.yanny.ali.api;

import com.yanny.aci.api.ICoreServerRegistry;
import com.yanny.aci.api.NumberConverter;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.plugin.glm.IEntitySubPredicateResolver;
import com.yanny.ali.plugin.glm.ILootContextPreparer;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.glm.IPageResolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.predicates.entity.EntitySubPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface IServerRegistry extends ICoreServerRegistry<IServerUtils> {
    <T extends LootPoolEntryContainer> void registerEntry(Class<T> type, EntryFactory<T> entryFactory);

    <T extends LootPoolEntryContainer> void registerEntryWeight(Class<T> type, NumberConverter<IServerUtils, T> weight);

    <T extends LootPoolEntryContainer> void registerEntryChildren(Class<T> type, BiFunction<IServerUtils, T, List<LootPoolEntryContainer>> children);

    <T extends LootPoolEntryContainer> void registerEntryTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends LootItemFunction> void registerFunctionTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends LootItemCondition> void registerConditionTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends Ingredient> void registerIngredientTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends DataComponentPredicate> void registerDataComponentPredicateTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    /**
     * Registers an unwrapper that turns a loader-specific composite {@link Ingredient} into the object actually
     * describing it, so that its tooltip can be looked up as a value tooltip. Needed on loaders where custom
     * ingredients are not {@link Ingredient} subclasses (NeoForge's {@code ICustomIngredient}) and therefore cannot be
     * dispatched by {@link #registerIngredientTooltip}. Returns {@code null} for ingredients it does not handle.
     */
    void registerIngredientUnwrapper(Function<Ingredient, Object> unwrapper);

    <T extends EntitySubPredicate> void registerEntitySubPredicateTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T> void registerDataComponentTypeTooltip(DataComponentType<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends ConsumeEffect> void registerConsumeEffectTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends SlotSource> void registerSlotSourceTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends ContextIntProvider> void registerContextIntProvider(Class<T> type, NumberConverter<IServerUtils, T> converter);

    <T extends ContextFloatProvider> void registerContextFloatProvider(Class<T> type, NumberConverter<IServerUtils, T> converter);

    <T extends LevelBasedValue> void registerLevelBasedValue(Class<T> type, TriFunction<IServerUtils, T, NumberExpr, NumberExpr> converter);

    <T extends LootItemFunction> void registerCountModifier(Class<T> type, NumberModifier<T> modifier);

    <T extends LootItemCondition> void registerChanceModifier(Class<T> type, NumberModifier<T> modifier);

    <T extends LootItemFunction> void registerItemStackModifier(Class<T> type, TriFunction<IServerUtils, T, ItemStack, ItemStack> consumer);

    <T extends LootItemFunction> void registerFunctionUnwrapper(Class<T> type, BiFunction<IServerUtils, T, List<LootItemFunction>> unwrapper);

    <T extends LootItemCondition> void registerConditionUnwrapper(Class<T> type, BiFunction<IServerUtils, T, List<LootItemCondition>> unwrapper);

    <T> void registerPageResolver(Class<T> type, IPageResolver<T> resolver);

    <T extends EntitySubPredicate> void registerEntitySubPredicateResolver(Class<T> type, IEntitySubPredicateResolver<T> resolver);

    void registerLootContextPreparer(ILootContextPreparer preparer);

    void registerLootModifiers(Function<IServerUtils, List<ILootModifier<?>>> getter);

    void registerGlobalLootModifiers(Function<IServerUtils, List<IPageLootModifier>> getter);

    /**
     * Registers a trader, so that its trades are scanned and listed under their own entry. A {@link TradeLevel.OfSet} is
     * looked up in the {@code minecraft:trade_set} registry when the scan runs, so it may name a set that a datapack
     * provides; a {@link TradeLevel.OfTrades} carries trades a mod defines in code. {@code levels} is called when the
     * scan runs, so it may read the server's registries through the {@link IServerUtils} it is given.
     */
    void registerTrades(Identifier traderId, @Nullable EntityType<?> entityType, Function<IServerUtils, Int2ObjectMap<TradeLevel>> levels);

    void registerTradeOverride(BiFunction<IServerUtils, Identifier, @Nullable Int2ObjectMap<TradeLevel>> override);

    @FunctionalInterface
    interface EntryFactory<T extends LootPoolEntryContainer> {
        IDataNode create(IServerUtils utils, T entry, NumberExpr chance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions);
    }
}
