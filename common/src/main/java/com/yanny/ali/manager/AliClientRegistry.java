package com.yanny.ali.manager;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.yanny.ali.plugin.client.WidgetUtils.GROUP_WIDGET_WIDTH;
import static com.yanny.ali.plugin.client.WidgetUtils.VERTICAL_OFFSET;

public class AliClientRegistry implements IClientRegistry, IClientUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LootContext LOOT_CONTEXT = new LootContext(new LootParams(null, Map.of(), Map.of(), 0F), RandomSource.create(), null);

    private final Map<LootPoolEntryType, IWidgetFactory> widgetMap = new HashMap<>();
    private final Map<LootNumberProviderType, BiFunction<IClientUtils, NumberProvider, RangeValue>> numberConverterMap = new HashMap<>();
    private final Map<LootItemConditionType, TriFunction<IClientUtils, Integer, LootItemCondition, List<Component>>> conditionTooltipMap = new HashMap<>();
    private final Map<LootItemFunctionType, TriFunction<IClientUtils, Integer, LootItemFunction, List<Component>>> functionTooltipMap = new HashMap<>();
    private final Map<LootItemConditionType, TriConsumer<IClientUtils, LootItemCondition, Map<Enchantment, Map<Integer, RangeValue>>>> chanceModifierMap = new HashMap<>();
    private final Map<LootItemFunctionType, TriConsumer<IClientUtils, LootItemFunction, Map<Enchantment, Map<Integer, RangeValue>>>> countModifierMap = new HashMap<>();
    private final Map<LootItemFunctionType, TriFunction<IClientUtils, LootItemFunction, ItemStack, ItemStack>> itemStackModifierMap = new HashMap<>();
    private final Map<LootPoolEntryType, WidgetDirection> widgetDirectionMap = new HashMap<>();
    private final Map<LootPoolEntryType, IBoundsGetter> widgetBoundsMap = new HashMap<>();
    private final Map<ResourceLocation, LootTable> lootTableMap = new HashMap<>();
    private final Map<ResourceLocation, List<Item>> lootItemMap = new HashMap<>();

    public void addLootTable(ResourceLocation resourceLocation, LootTable lootTable, List<Item> items) {
        lootTableMap.put(resourceLocation, lootTable);
        lootItemMap.put(resourceLocation, items);
    }

    public Map<ResourceLocation, LootTable> getLootTables() {
        return lootTableMap;
    }

    public void clearLootTables() {
        lootTableMap.clear();
    }

    @Override
    public void registerWidget(LootPoolEntryType type, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter) {
        widgetMap.put(type, factory);
        widgetDirectionMap.put(type, direction);
        widgetBoundsMap.put(type, boundsGetter);
    }

    @Override
    public <T extends NumberProvider> void registerNumberProvider(LootNumberProviderType type, BiFunction<IClientUtils, T, RangeValue> converter) {
        //noinspection unchecked
        numberConverterMap.put(type, (u, t) -> converter.apply(u, (T) t));
    }

    @Override
    public <T extends LootItemCondition> void registerConditionTooltip(LootItemConditionType type, TriFunction<IClientUtils, Integer, T, List<Component>> getter) {
        //noinspection unchecked
        conditionTooltipMap.put(type, (u, i, c) -> getter.apply(u, i, (T) c));
    }

    @Override
    public <T extends LootItemFunction> void registerFunctionTooltip(LootItemFunctionType type, TriFunction<IClientUtils, Integer, T, List<Component>> getter) {
        //noinspection unchecked
        functionTooltipMap.put(type, (u, i, f) -> getter.apply(u, i, (T) f));
    }

    @Override
    public <T extends LootItemFunction> void registerCountModifier(LootItemFunctionType type, TriConsumer<IClientUtils, T, Map<Enchantment, Map<Integer, RangeValue>>> consumer) {
        //noinspection unchecked
        countModifierMap.put(type, (u, f, v) -> consumer.accept(u, (T) f, v));
    }

    @Override
    public <T extends LootItemCondition> void registerChanceModifier(LootItemConditionType type, TriConsumer<IClientUtils, T, Map<Enchantment, Map<Integer, RangeValue>>> consumer) {
        //noinspection unchecked
        chanceModifierMap.put(type, (u, f, v) -> consumer.accept(u, (T) f, v));
    }

    @Override
    public <T extends LootItemFunction> void registerItemStackModifier(LootItemFunctionType type, TriFunction<IClientUtils, T, ItemStack, ItemStack> consumer) {
        //noinspection unchecked
        itemStackModifierMap.put(type, (u, f, i) -> consumer.apply(u, (T) f, i));
    }

    @Override
    public Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils utils, List<LootPoolEntryContainer> entries, int x, int y, int maxWidth,
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
            WidgetDirection direction = widgetDirectionMap.get(entry.getType());
            IWidgetFactory widgetFactory = widgetMap.get(entry.getType());
            IBoundsGetter bounds = widgetBoundsMap.get(entry.getType());

            if (direction != null && widgetFactory != null && bounds != null) {
                if (lastDirection != null && direction != lastDirection && direction == WidgetDirection.VERTICAL) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY = y + height + VERTICAL_OFFSET;
                }

                Rect bound = bounds.apply(utils, entry, posX, posY, maxWidth);

                if (bound.right() > maxWidth) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY += bound.height();
                    bound = bounds.apply(utils, entry, posX, posY, maxWidth);
                }

                IEntryWidget widget = widgetFactory.create(utils, entry, posX, posY, maxWidth, sumWeight, List.copyOf(functions), List.copyOf(conditions));
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
    public <T extends LootItemCondition> List<Component> getConditionTooltip(IClientUtils utils, int pad, T condition) {
        TriFunction<IClientUtils, Integer, LootItemCondition, List<Component>> entryTooltipGetter = conditionTooltipMap.get(condition.getType());

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, pad, condition);
        } else {
            LOGGER.warn("Condition tooltip for {} was not registered", condition.getClass().getCanonicalName());
            return List.of();
        }
    }

    @Override
    public <T extends LootItemFunction> List<Component> getFunctionTooltip(IClientUtils utils, int pad, T function) {
        TriFunction<IClientUtils, Integer, LootItemFunction, List<Component>> entryTooltipGetter = functionTooltipMap.get(function.getType());

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, pad, function);
        } else {
            LOGGER.warn("Function tooltip for {} was not registered", function.getClass().getCanonicalName());
            return List.of();
        }
    }

    @Override
    public <T extends LootItemFunction> void applyCountModifier(IClientUtils utils, T function, Map<Enchantment, Map<Integer, RangeValue>> count) {
        TriConsumer<IClientUtils, LootItemFunction, Map<Enchantment, Map<Integer, RangeValue>>> bonusCountConsumer = countModifierMap.get(function.getType());

        if (bonusCountConsumer != null) {
            bonusCountConsumer.accept(utils, function, count);
        }
    }

    @Override
    public <T extends LootItemCondition> void applyChanceModifier(IClientUtils utils, T condition, Map<Enchantment, Map<Integer, RangeValue>> chance) {
        TriConsumer<IClientUtils, LootItemCondition, Map<Enchantment, Map<Integer, RangeValue>>> bonusChanceConsumer = chanceModifierMap.get(condition.getType());

        if (bonusChanceConsumer != null) {
            bonusChanceConsumer.accept(utils, condition, chance);
        }
    }

    @Override
    public <T extends LootItemFunction> ItemStack applyItemStackModifier(IClientUtils utils, T function, ItemStack itemStack) {
        TriFunction<IClientUtils, LootItemFunction, ItemStack, ItemStack> bonusChanceConsumer = itemStackModifierMap.get(function.getType());

        if (bonusChanceConsumer != null) {
            itemStack = bonusChanceConsumer.apply(utils, function, itemStack);
        }

        return itemStack;
    }

    @Override
    public Rect getBounds(IClientUtils utils, List<LootPoolEntryContainer> entries, int x, int y, int maxWidth) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;
        WidgetDirection lastDirection = null;

        for (LootPoolEntryContainer entry : entries) {
            WidgetDirection direction = widgetDirectionMap.get(entry.getType());
            IWidgetFactory widgetFactory = widgetMap.get(entry.getType());
            IBoundsGetter bounds = widgetBoundsMap.get(entry.getType());

            if (direction != null && widgetFactory != null && bounds != null) {
                if (lastDirection != null && direction != lastDirection && direction == WidgetDirection.VERTICAL) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY = y + height + VERTICAL_OFFSET;
                }

                Rect bound = bounds.apply(utils, entry, posX, posY, maxWidth);

                if (bound.right() > maxWidth) {
                    posX = x + GROUP_WIDGET_WIDTH;
                    posY += bound.height();
                    bound = bounds.apply(utils, entry, posX, posY, maxWidth);
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
        return widgetDirectionMap.get(entry.getType());
    }

    @Override
    public LootContext getLootContext() {
        return LOOT_CONTEXT;
    }

    @Override
    public List<Item> getItems(ResourceLocation location) {
        return lootItemMap.getOrDefault(location, List.of());
    }

    @Override
    public List<LootPool> getLootPools(LootTable lootTable) {
        return Services.PLATFORM.getLootPools(lootTable);
    }

    @Nullable
    @Override
    public LootTable getLootTable(ResourceLocation resourceLocation) {
        return lootTableMap.get(resourceLocation);
    }

    @Override
    public RangeValue convertNumber(IClientUtils utils, @Nullable NumberProvider numberProvider) {
        if (numberProvider != null) {
            BiFunction<IClientUtils, NumberProvider, RangeValue> function = numberConverterMap.get(numberProvider.getType());

            if (function != null) {
                try {
                    return function.apply(utils, numberProvider);
                } catch (Throwable throwable) {
                    LOGGER.warn("Failed to convert number with error {}", throwable.getMessage());
                }
            } else {
                LOGGER.warn("Number converter for {} was not registered", numberProvider.getClass().getCanonicalName());
            }
        }

        return new RangeValue(false, true);
    }

    public void printClientInfo() {
        LOGGER.info("Registered {} widgets", widgetMap.size());
        LOGGER.info("Registered {} number converters", numberConverterMap.size());
        LOGGER.info("Registered {} condition tooltips", conditionTooltipMap.size());
        LOGGER.info("Registered {} function tooltips", functionTooltipMap.size());
    }
}
