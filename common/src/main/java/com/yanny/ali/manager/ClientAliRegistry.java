package com.yanny.ali.manager;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.entry.SingletonEntry;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.yanny.ali.plugin.WidgetUtils.GROUP_WIDGET_WIDTH;
import static com.yanny.ali.plugin.WidgetUtils.VERTICAL_OFFSET;

public class ClientAliRegistry implements IClientRegistry, IClientUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<Class<?>, IWidgetFactory> widgetMap = new HashMap<>();
    private final Map<Class<?>, TriFunction<IUtils, Integer, ILootCondition, List<Component>>> conditionTooltipMap = new HashMap<>();
    private final Map<Class<?>, TriFunction<IUtils, Integer, ILootFunction, List<Component>>> functionTooltipMap = new HashMap<>();
    private final Map<Class<?>, TriFunction<IUtils, Integer, ILootEntry, List<Component>>> entryTooltipMap = new HashMap<>();
    private final Map<Class<?>, WidgetDirection> widgetDirectionMap = new HashMap<>();
    private final Map<Class<?>, IBoundsGetter> widgetBoundsMap = new HashMap<>();

    @Override
    public <T extends ILootEntry> void registerWidget(Class<T> clazz, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter) {
        widgetMap.put(clazz, factory);
        widgetDirectionMap.put(clazz, direction);
        widgetBoundsMap.put(clazz, boundsGetter);
    }

    @Override
    public <T extends ILootCondition> void registerConditionTooltip(Class<T> clazz, TriFunction<IUtils, Integer, ILootCondition, List<Component>> getter) {
        conditionTooltipMap.put(clazz, getter);
    }

    @Override
    public <T extends ILootFunction> void registerFunctionTooltip(Class<T> clazz, TriFunction<IUtils, Integer, ILootFunction, List<Component>> getter) {
        functionTooltipMap.put(clazz, getter);
    }

    @Override
    public <T extends ILootEntry> void registerEntryTooltip(Class<T> clazz, TriFunction<IUtils, Integer, ILootEntry, List<Component>> getter) {
        entryTooltipMap.put(clazz, getter);
    }

    @Override
    public Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils utils, List<ILootEntry> entries, int x, int y,
                                                        List<ILootFunction> functions, List<ILootCondition> conditions) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;
        int sumWeight = 0;
        List<IEntryWidget> widgets = new LinkedList<>();
        WidgetDirection lastDirection = null;

        for (ILootEntry entry : entries) {
            if (entry instanceof SingletonEntry singletonEntry) {
                sumWeight += singletonEntry.weight;
            }
        }

        for (ILootEntry entry : entries) {
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
    public <T extends ILootCondition> List<Component> getConditionTooltip(Class<T> clazz, IUtils utils, int pad, ILootCondition condition) {
        TriFunction<IUtils, Integer, ILootCondition, List<Component>> entryTooltipGetter = conditionTooltipMap.get(clazz);

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, pad, condition);
        } else {
            LOGGER.warn("Condition tooltip {} was not registered", clazz.getCanonicalName());
            return List.of();
        }
    }

    @Override
    public <T extends ILootFunction> List<Component> getFunctionTooltip(Class<T> clazz, IUtils utils, int pad, ILootFunction function) {
        TriFunction<IUtils, Integer, ILootFunction, List<Component>> entryTooltipGetter = functionTooltipMap.get(clazz);

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, pad, function);
        } else {
            LOGGER.warn("Function tooltip {} was not registered", clazz.getCanonicalName());
            return List.of();
        }
    }

    @Override
    public <T extends ILootEntry> List<Component> getEntryTooltip(Class<T> clazz, IUtils utils, int pad, ILootEntry entry) {
        TriFunction<IUtils, Integer, ILootEntry, List<Component>> entryTooltipGetter = entryTooltipMap.get(clazz);

        if (entryTooltipGetter != null) {
            return entryTooltipGetter.apply(utils, pad, entry);
        } else {
            LOGGER.warn("Entry tooltip {} was not registered", clazz.getCanonicalName());
            return List.of();
        }
    }

    @Override
    public Rect getBounds(IClientUtils utils, List<ILootEntry> entries, int x, int y) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;
        WidgetDirection lastDirection = null;

        for (ILootEntry entry : entries) {
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
    public WidgetDirection getWidgetDirection(ILootEntry entry) {
        return widgetDirectionMap.get(entry.getClass());
    }

    public void printClientInfo() {
        LOGGER.info("Registered {} widgets", widgetMap.size());
    }
}
