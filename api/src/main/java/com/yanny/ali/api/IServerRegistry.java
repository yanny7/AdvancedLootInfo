package com.yanny.ali.api;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface IServerRegistry {
    <T extends LootPoolEntryContainer> void registerItemCollector(Class<?> type, BiFunction<IServerUtils, T, List<Item>> itemSupplier);

    <T extends LootItemFunction> void registerItemCollector(Class<T> type, TriFunction<IServerUtils, List<Item>, T, List<Item>> itemSupplier);

    <T extends LootPoolEntryContainer> void registerEntry(Class<T> type, EntryFactory<T> entryFactory);

    <T extends LootItemFunction> void registerFunctionTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends LootItemCondition> void registerConditionTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends Ingredient> void registerIngredientTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends DataComponentPredicate> void registerDataComponentPredicateTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends EntitySubPredicate> void registerEntitySubPredicateTooltip(MapCodec<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T> void registerDataComponentTypeTooltip(DataComponentType<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends ConsumeEffect> void registerConsumeEffectTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends NumberProvider> void registerNumberProvider(Class<T> type, BiFunction<IServerUtils, T, RangeValue> converter);

    <T extends LootItemFunction> void registerCountModifier(Class<T> type, TriConsumer<IServerUtils, T, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> consumer);

    <T extends LootItemCondition> void registerChanceModifier(Class<T> type, TriConsumer<IServerUtils, T, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> consumer);

    <T extends LootItemFunction> void registerItemStackModifier(Class<T> type, TriFunction<IServerUtils, T, ItemStack, ItemStack> consumer);

    void registerLootModifiers(Function<IServerUtils, List<ILootModifier<?>>> getter);

    @FunctionalInterface
    interface EntryFactory<T extends LootPoolEntryContainer> {
        IDataNode create(IServerUtils utils, T entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions);
    }
}
