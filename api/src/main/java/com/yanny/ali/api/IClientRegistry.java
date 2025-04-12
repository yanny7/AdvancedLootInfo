package com.yanny.ali.api;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;

import java.util.List;
import java.util.function.BiFunction;

public interface IClientRegistry {
    void registerWidget(LootPoolEntryType type, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter);
    <T extends NumberProvider> void registerNumberProvider(LootNumberProviderType type, BiFunction<IClientUtils, T, RangeValue> converter);

    <T extends LootItemCondition> void registerConditionTooltip(LootItemConditionType type, TriFunction<IClientUtils, Integer, T, List<Component>> getter);
    <T extends LootItemFunction> void registerFunctionTooltip(LootItemFunctionType<T> type, TriFunction<IClientUtils, Integer, T, List<Component>> getter);
    <T extends ItemSubPredicate> void registerItemSubPredicateTooltip(ItemSubPredicate.Type<T> type, TriFunction<IClientUtils, Integer, T, List<Component>> getter);
    <T extends EntitySubPredicate> void registerEntitySubPredicateTooltip(MapCodec<T> type, TriFunction<IClientUtils, Integer, T, List<Component>> getter);

    @FunctionalInterface
    interface IBoundsGetter {
        Rect apply(IClientUtils utils, LootPoolEntryContainer entry, int x, int y);
    }

    @FunctionalInterface
    interface IWidgetFactory {
        IEntryWidget create(IWidgetUtils registry, LootPoolEntryContainer entry, int x, int y, int totalWeight,
                            List<LootItemFunction> functions, List<LootItemCondition> conditions);
    }
}
