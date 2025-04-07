package com.yanny.ali.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
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

public interface IRegistry {
    void registerWidget(LootPoolEntryType type, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter);
    void registerNumberProvider(LootNumberProviderType type, BiFunction<IUtils, NumberProvider, RangeValue> converter);

    <T extends LootItemCondition> void registerConditionTooltip(LootItemConditionType type, TriFunction<IUtils, Integer, T, List<Component>> getter);
    <T extends LootItemFunction> void registerFunctionTooltip(LootItemFunctionType type, TriFunction<IUtils, Integer, T, List<Component>> getter);
    <T extends LootPoolEntryContainer> void registerItemCollector(LootPoolEntryType type, BiFunction<IUtils, T, List<Item>> itemSupplier);

    @FunctionalInterface
    interface IBoundsGetter {
        Rect apply(IUtils utils, LootPoolEntryContainer entry, int x, int y);
    }

    @FunctionalInterface
    interface IWidgetFactory {
        IEntryWidget create(IWidgetUtils registry, LootPoolEntryContainer entry, int x, int y, int totalWeight,
                            List<LootItemFunction> functions, List<LootItemCondition> conditions);
    }
}
