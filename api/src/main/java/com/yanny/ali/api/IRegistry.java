package com.yanny.ali.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;

import java.util.List;
import java.util.function.BiFunction;

public interface IRegistry {
    <T extends LootPoolEntryContainer> void registerWidget(Class<T> clazz, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter);
    void registerNumberProvider(ResourceLocation key, BiFunction<IUtils, NumberProvider, RangeValue> converter);

    <T extends LootItemCondition> void registerConditionTooltip(Class<T> clazz, TriFunction<IUtils, Integer, LootItemCondition, List<Component>> getter);
    <T extends LootItemFunction> void registerFunctionTooltip(Class<T> clazz, TriFunction<IUtils, Integer, LootItemFunction, List<Component>> getter);
    <T extends LootPoolEntryContainer> void registerItemCollector(Class<T> clazz, BiFunction<IUtils, LootPoolEntryContainer, List<Item>> itemSupplier);

    interface IBoundsGetter {
        Rect apply(IUtils utils, LootPoolEntryContainer entry, int x, int y);
    }
}
