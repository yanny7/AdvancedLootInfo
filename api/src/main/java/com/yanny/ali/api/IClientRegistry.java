package com.yanny.ali.api;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
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

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public interface IClientRegistry {
    void registerWidget(LootPoolEntryType type, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter);

    <T extends NumberProvider> void registerNumberProvider(LootNumberProviderType type, BiFunction<IClientUtils, T, RangeValue> converter);

    <T extends LootItemCondition> void registerConditionTooltip(LootItemConditionType type, TriFunction<IClientUtils, Integer, T, List<Component>> getter);

    <T extends LootItemFunction> void registerFunctionTooltip(LootItemFunctionType<T> type, TriFunction<IClientUtils, Integer, T, List<Component>> getter);

    <T extends ItemSubPredicate> void registerItemSubPredicateTooltip(ItemSubPredicate.Type<T> type, TriFunction<IClientUtils, Integer, T, List<Component>> getter);

    <T extends EntitySubPredicate> void registerEntitySubPredicateTooltip(MapCodec<T> type, TriFunction<IClientUtils, Integer, T, List<Component>> getter);

    <T> void registerDataComponentTypeTooltip(DataComponentType<T> type, TriFunction<IClientUtils, Integer, T, List<Component>> getter);

    <T extends LootItemFunction> void registerCountModifier(LootItemFunctionType<T> type, TriConsumer<IClientUtils, T, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> consumer);

    <T extends LootItemCondition> void registerChanceModifier(LootItemConditionType type, TriConsumer<IClientUtils, T, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> consumer);

    <T extends LootItemFunction> void registerItemStackModifier(LootItemFunctionType<T> type, TriFunction<IClientUtils, T, ItemStack, ItemStack> consumer);

    @FunctionalInterface
    interface IBoundsGetter {
        Rect apply(IClientUtils utils, LootPoolEntryContainer entry, int x, int y, int maxWidth);
    }

    @FunctionalInterface
    interface IWidgetFactory {
        IEntryWidget create(IWidgetUtils registry, LootPoolEntryContainer entry, int x, int y, int maxWidth, int totalWeight,
                            List<LootItemFunction> functions, List<LootItemCondition> conditions);
    }

    @FunctionalInterface
    interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }
}
