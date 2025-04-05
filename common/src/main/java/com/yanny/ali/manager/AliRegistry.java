package com.yanny.ali.manager;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.yanny.ali.plugin.WidgetUtils.GROUP_WIDGET_WIDTH;
import static com.yanny.ali.plugin.WidgetUtils.VERTICAL_OFFSET;

public class AliRegistry implements IRegistry, IUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LootContext LOOT_CONTEXT = new LootContext(new LootParams(null, Map.of(), Map.of(), 0F), RandomSource.create(), null);

    private final Map<Class<?>, IWidgetFactory> widgetMap = new HashMap<>();
    private final Map<ResourceLocation, BiFunction<IUtils, NumberProvider, RangeValue>> numberConverterMap = new HashMap<>();
    private final Map<Class<?>, TriFunction<IUtils, Integer, LootItemCondition, List<Component>>> conditionTooltipMap = new HashMap<>();
    private final Map<Class<?>, TriFunction<IUtils, Integer, LootItemFunction, List<Component>>> functionTooltipMap = new HashMap<>();
    private final Map<Class<?>, BiFunction<IUtils, LootPoolEntryContainer, List<Item>>> itemCollectorMap = new HashMap<>();
    private final Map<Class<?>, WidgetDirection> widgetDirectionMap = new HashMap<>();
    private final Map<Class<?>, IBoundsGetter> widgetBoundsMap = new HashMap<>();
    private final Map<ResourceKey<LootTable>, LootTable> lootTableMap = new HashMap<>();

    public void addLootTable(ResourceKey<LootTable> resourceLocation, LootTable lootTable) {
        lootTableMap.put(resourceLocation, lootTable);
    }

    public Map<ResourceKey<LootTable>, LootTable> getLootTables() {
        return lootTableMap;
    }

    public void clearLootTables() {
        lootTableMap.clear();
    }

    @Override
    public <T extends LootPoolEntryContainer> void registerWidget(Class<T> clazz, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter) {
        widgetMap.put(clazz, factory);
        widgetDirectionMap.put(clazz, direction);
        widgetBoundsMap.put(clazz, boundsGetter);
    }

    @Override
    public void registerNumberProvider(ResourceLocation key, BiFunction<IUtils, NumberProvider, RangeValue> converter) {
        numberConverterMap.put(key, converter);
    }

    @Override
    public <T extends LootItemCondition> void registerConditionTooltip(Class<T> clazz, TriFunction<IUtils, Integer, LootItemCondition, List<Component>> getter) {
        conditionTooltipMap.put(clazz, getter);
    }

    @Override
    public <T extends LootItemFunction> void registerFunctionTooltip(Class<T> clazz, TriFunction<IUtils, Integer, LootItemFunction, List<Component>> getter) {
        functionTooltipMap.put(clazz, getter);
    }

    @Override
    public <T extends LootPoolEntryContainer> void registerItemCollector(Class<T> clazz, BiFunction<IUtils, LootPoolEntryContainer, List<Item>> itemSupplier) {
        itemCollectorMap.put(clazz, itemSupplier);
    }

    @Override
    public Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils utils, List<LootPoolEntryContainer> entries, int x, int y,
                                                        List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;
        int sumWeight = 0;
        List<IEntryWidget> widgets = new LinkedList<>();
        WidgetDirection lastDirection = null;

        for (LootPoolEntryContainer entry : entries) {
            if (entry instanceof LootPoolSingletonContainer singletonEntry) {
                sumWeight += singletonEntry.weight;
            }
        }

        for (LootPoolEntryContainer entry : entries) {
            WidgetDirection direction = widgetDirectionMap.get(entry.getClass());
            IWidgetFactory widgetFactory = widgetMap.get(entry.getClass());
            IBoundsGetter bounds = widgetBoundsMap.get(entry.getClass());

            if (direction != null && widgetFactory != null && bounds != null) {
                if (lastDirection != null && direction != lastDirection && direction == WidgetDirection.VERTICAL) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY = y + height + VERTICAL_OFFSET;
                }

                Rect bound = bounds.apply(utils, entry, posX, posY);

                if (bound.right() > 9 * 18) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY += bound.height();
                    bound = bounds.apply(utils, entry, posX, posY);
                }

                IEntryWidget widget = widgetFactory.create(utils, entry, posX, posY, sumWeight, List.copyOf(functions), List.copyOf(conditions));
                width = Math.max(width, bound.right() - x);
                height = Math.max(height, bound.bottom() - y);

                if (lastDirection != null) {
                    if (lastDirection != direction) {
                        posX = x + GROUP_WIDGET_WIDTH;
                        posY += bound.height() + VERTICAL_OFFSET;
                    } else if (direction == WidgetDirection.HORIZONTAL) {
                        posX += bound.width();
                    } else if (direction == WidgetDirection.VERTICAL) {
                        posY += bound.height() + VERTICAL_OFFSET;
                    }
                } else {
                    switch (direction) {
                        case HORIZONTAL -> posX += bound.width();
                        case VERTICAL -> posY += bound.height() + VERTICAL_OFFSET;
                    }
                }

                widgets.add(widget);
                lastDirection = direction;
            }
        }

        return new Pair<>(widgets, new Rect(x, y, width, height));
    }

    @Override
    public <T extends LootItemCondition> List<Component> getConditionTooltip(Class<T> clazz, IUtils utils, int pad, LootItemCondition condition) {
        TriFunction<IUtils, Integer, LootItemCondition, List<Component>> entryTooltipGetter = conditionTooltipMap.get(clazz);

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, pad, condition);
        } else {
            LOGGER.warn("Condition tooltip {} was not registered", clazz.getCanonicalName());
            return List.of();
        }
    }

    @Override
    public <T extends LootItemFunction> List<Component> getFunctionTooltip(Class<T> clazz, IUtils utils, int pad, LootItemFunction function) {
        TriFunction<IUtils, Integer, LootItemFunction, List<Component>> entryTooltipGetter = functionTooltipMap.get(clazz);

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, pad, function);
        } else {
            LOGGER.warn("Function tooltip {} was not registered", clazz.getCanonicalName());
            return List.of();
        }
    }

    @Override
    public <T extends LootPoolEntryContainer> List<Item> collectItems(Class<T> clazz, IUtils utils, LootPoolEntryContainer entry) {
        BiFunction<IUtils, LootPoolEntryContainer, List<Item>> itemSupplier = itemCollectorMap.get(entry.getClass());

        if (itemSupplier != null) {
            return itemSupplier.apply(utils, entry);
        } else {
            LOGGER.warn("Item collector {} was not registered", clazz.getCanonicalName());
            return List.of();
        }
    }

    @Override
    public Rect getBounds(IUtils utils, List<LootPoolEntryContainer> entries, int x, int y) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;
        WidgetDirection lastDirection = null;

        for (LootPoolEntryContainer entry : entries) {
            WidgetDirection direction = widgetDirectionMap.get(entry.getClass());
            IWidgetFactory widgetFactory = widgetMap.get(entry.getClass());
            IBoundsGetter bounds = widgetBoundsMap.get(entry.getClass());

            if (direction != null && widgetFactory != null && bounds != null) {
                if (lastDirection != null && direction != lastDirection && direction == WidgetDirection.VERTICAL) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY = y + height + VERTICAL_OFFSET;
                }

                Rect bound = bounds.apply(utils, entry, posX, posY);

                if (bound.right() > 9 * 18) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY += bound.height();
                    bound = bounds.apply(utils, entry, posX, posY);
                }

                width = Math.max(width, bound.right() - x);
                height = Math.max(height, bound.bottom() - y);

                if (lastDirection != null) {
                    if (lastDirection != direction) {
                        posX = x + GROUP_WIDGET_WIDTH;
                        posY += bound.height() + VERTICAL_OFFSET;
                    } else if (direction == WidgetDirection.HORIZONTAL) {
                        posX += bound.width();
                    } else if (direction == WidgetDirection.VERTICAL) {
                        posY += bound.height() + VERTICAL_OFFSET;
                    }
                } else {
                    switch (direction) {
                        case HORIZONTAL -> posX += bound.width();
                        case VERTICAL -> posY += bound.height() + VERTICAL_OFFSET;
                    }
                }

                lastDirection = direction;
            }
        }

        return new Rect(x, y, width, height);
    }

    @Nullable
    @Override
    public WidgetDirection getWidgetDirection(LootPoolEntryContainer entry) {
        return widgetDirectionMap.get(entry.getClass());
    }

    @Override
    public LootContext getLootContext() {
        return LOOT_CONTEXT;
    }

    @Nullable
    @Override
    public LootTable getLootTable(Either<ResourceKey<LootTable>, LootTable> either) {
        return either.map(lootTableMap::get, lootTable -> lootTable);
    }

    @Override
    public RangeValue convertNumber(IUtils utils, @Nullable NumberProvider numberProvider) {
        if (numberProvider != null) {
            ResourceLocation key = BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE.getKey(numberProvider.getType());

            if (key != null) {
                BiFunction<IUtils, NumberProvider, RangeValue> function = numberConverterMap.get(key);

                if (function != null) {
                    try {
                        return function.apply(utils, numberProvider);
                    } catch (Throwable throwable) {
                        LOGGER.warn("Failed to convert number with error {}", throwable.getMessage());
                    }
                } else {
                    LOGGER.warn("Number converter {} was not registered", key);
                }
            }

        }

        return new RangeValue(false, true);
    }

    public void printClientInfo() {
        LOGGER.info("Registered {} widgets", widgetMap.size());
    }
}
