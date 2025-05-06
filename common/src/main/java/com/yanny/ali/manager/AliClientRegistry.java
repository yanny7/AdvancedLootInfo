package com.yanny.ali.manager;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.yanny.ali.api.*;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
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
    private final Map<LootItemFunctionType<?>, TriFunction<IClientUtils, Integer, LootItemFunction, List<Component>>> functionTooltipMap = new HashMap<>();
    private final Map<ItemSubPredicate.Type<?>, TriFunction<IClientUtils, Integer, ItemSubPredicate, List<Component>>> itemSubPredicateTooltipMap = new HashMap<>();
    private final Map<MapCodec<?>, TriFunction<IClientUtils, Integer, EntitySubPredicate, List<Component>>> entitySubPredicateTooltipMap = new HashMap<>();
    private final Map<DataComponentType<?>, TriFunction<IClientUtils, Integer, Object, List<Component>>> dataComponentTypeTooltipMap = new HashMap<>();
    private final Map<LootItemConditionType, TriConsumer<IClientUtils, LootItemCondition, Map<Holder<Enchantment>, Map<Integer, RangeValue>>>> chanceModifierMap = new HashMap<>();
    private final Map<LootItemFunctionType<?>, TriConsumer<IClientUtils, LootItemFunction, Map<Holder<Enchantment>, Map<Integer, RangeValue>>>> countModifierMap = new HashMap<>();
    private final Map<LootItemFunctionType<?>, TriFunction<IClientUtils, LootItemFunction, ItemStack, ItemStack>> itemStackModifierMap = new HashMap<>();
    private final Map<LootPoolEntryType, WidgetDirection> widgetDirectionMap = new HashMap<>();
    private final Map<LootPoolEntryType, IBoundsGetter> widgetBoundsMap = new HashMap<>();
    private final Map<ResourceKey<LootTable>, LootTable> lootTableMap = new HashMap<>();
    private final Map<ResourceKey<LootTable>, List<Item>> lootItemMap = new HashMap<>();

    public void addLootTable(ResourceKey<LootTable> resourceLocation, LootTable lootTable, List<Item> items) {
        lootTableMap.put(resourceLocation, lootTable);
        lootItemMap.put(resourceLocation, items);
    }

    public Map<ResourceKey<LootTable>, LootTable> getLootTables() {
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
    public <T extends LootItemFunction> void registerFunctionTooltip(LootItemFunctionType<T> type, TriFunction<IClientUtils, Integer, T, List<Component>> getter) {
        //noinspection unchecked
        functionTooltipMap.put(type, (u, i, f) -> getter.apply(u, i, (T) f));
    }

    @Override
    public <T extends ItemSubPredicate> void registerItemSubPredicateTooltip(ItemSubPredicate.Type<T> type, TriFunction<IClientUtils, Integer, T, List<Component>> getter) {
        //noinspection unchecked
        itemSubPredicateTooltipMap.put(type, (u, i, f) -> getter.apply(u, i, (T) f));
    }

    @Override
    public <T extends EntitySubPredicate> void registerEntitySubPredicateTooltip(MapCodec<T> type, TriFunction<IClientUtils, Integer, T, List<Component>> getter) {
        //noinspection unchecked
        entitySubPredicateTooltipMap.put(type, (u, i, f) -> getter.apply(u, i, (T) f));
    }

    @Override
    public <T> void registerDataComponentTypeTooltip(DataComponentType<T> type, TriFunction<IClientUtils, Integer, T, List<Component>> getter) {
        //noinspection unchecked
        dataComponentTypeTooltipMap.put(type, (u, i, f) -> getter.apply(u, i, (T) f));
    }

    @Override
    public <T extends LootItemFunction> void registerCountModifier(LootItemFunctionType<T> type, TriConsumer<IClientUtils, T, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> consumer) {
        //noinspection unchecked
        countModifierMap.put(type, (u, f, v) -> consumer.accept(u, (T) f, v));
    }

    @Override
    public <T extends LootItemCondition> void registerChanceModifier(LootItemConditionType type, TriConsumer<IClientUtils, T, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> consumer) {
        //noinspection unchecked
        chanceModifierMap.put(type, (u, f, v) -> consumer.accept(u, (T) f, v));
    }

    @Override
    public <T extends LootItemFunction> void registerItemStackModifier(LootItemFunctionType<T> type, TriFunction<IClientUtils, T, ItemStack, ItemStack> consumer) {
        //noinspection unchecked
        itemStackModifierMap.put(type, (u, f, i) -> consumer.apply(u, (T) f, i));
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
            WidgetDirection direction = widgetDirectionMap.get(entry.getType());
            IWidgetFactory widgetFactory = widgetMap.get(entry.getType());
            IBoundsGetter bounds = widgetBoundsMap.get(entry.getType());

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
    public <T extends ItemSubPredicate> List<Component> getItemSubPredicateTooltip(IClientUtils utils, int pad, ItemSubPredicate.Type<?> type, T predicate) {
        TriFunction<IClientUtils, Integer, ItemSubPredicate, List<Component>> entryTooltipGetter = itemSubPredicateTooltipMap.get(type);

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, pad, predicate);
        } else {
            LOGGER.warn("ItemSubPredicate tooltip for {} was not registered", predicate.getClass().getCanonicalName());
            return List.of();
        }
    }

    @Override
    public <T extends EntitySubPredicate> List<Component> getEntitySubPredicateTooltip(IClientUtils utils, int pad, T predicate) {
        TriFunction<IClientUtils, Integer, EntitySubPredicate, List<Component>> entryTooltipGetter = entitySubPredicateTooltipMap.get(predicate.codec());

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, pad, predicate);
        } else {
            LOGGER.warn("EntitySubPredicate tooltip for {} was not registered", predicate.getClass().getCanonicalName());
            return List.of();
        }
    }

    @Override
    public List<Component> getDataComponentTypeTooltip(IClientUtils utils, int pad, DataComponentType<?> type, Object value) {
        TriFunction<IClientUtils, Integer, Object, List<Component>> getter = dataComponentTypeTooltipMap.get(type);

        if (getter != null) {
            return getter.apply(utils, pad, value);
        } else {
            LOGGER.warn("DataComponentType tooltip for {} was not registered", BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type));
            return List.of();
        }
    }

    @Override
    public <T extends LootItemFunction> void applyCountModifier(IClientUtils utils, T function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        TriConsumer<IClientUtils, LootItemFunction, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCountConsumer = countModifierMap.get(function.getType());

        if (bonusCountConsumer != null) {
            bonusCountConsumer.accept(utils, function, count);
        }
    }

    @Override
    public <T extends LootItemCondition> void applyChanceModifier(IClientUtils utils, T condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        TriConsumer<IClientUtils, LootItemCondition, Map<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChanceConsumer = chanceModifierMap.get(condition.getType());

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
    public Rect getBounds(IClientUtils utils, List<LootPoolEntryContainer> entries, int x, int y) {
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
        return widgetDirectionMap.get(entry.getType());
    }

    @Override
    public LootContext getLootContext() {
        return LOOT_CONTEXT;
    }

    @Override
    public List<Item> getItems(ResourceKey<LootTable> location) {
        return lootItemMap.getOrDefault(location, List.of());
    }

    @Nullable
    @Override
    public LootTable getLootTable(Either<ResourceKey<LootTable>, LootTable> either) {
        return either.map(lootTableMap::get, lootTable -> lootTable);
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

    @Nullable
    @Override
    public HolderLookup.Provider lookupProvider() {
        Level level = Minecraft.getInstance().level;

        return level != null ? level.registryAccess() : null;
    }

    public void printClientInfo() {
        LOGGER.info("Registered {} widgets", widgetMap.size());
        LOGGER.info("Registered {} number converters", numberConverterMap.size());
        LOGGER.info("Registered {} condition tooltips", conditionTooltipMap.size());
        LOGGER.info("Registered {} function tooltips", functionTooltipMap.size());
    }
}
