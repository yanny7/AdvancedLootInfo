package com.yanny.ali.api;

import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.function.TriFunction;

import java.util.List;

public interface IClientRegistry {
    <T extends ILootEntry> void registerWidget(Class<T> clazz, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter);

    <T extends ILootCondition> void registerConditionTooltip(Class<T> clazz, TriFunction<IUtils, Integer, ILootCondition, List<Component>> getter);
    <T extends ILootFunction> void registerFunctionTooltip(Class<T> clazz, TriFunction<IUtils, Integer, ILootFunction, List<Component>> getter);
    <T extends ILootEntry> void registerEntryTooltip(Class<T> clazz, TriFunction<IUtils, Integer, ILootEntry, List<Component>> getter);

    interface IBoundsGetter {
        Rect apply(IClientUtils registry, ILootEntry entry, int x, int y);
    }
}
