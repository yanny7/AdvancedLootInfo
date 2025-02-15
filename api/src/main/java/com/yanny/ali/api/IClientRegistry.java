package com.yanny.ali.api;

public interface IClientRegistry {
    <T extends ILootEntry> void registerWidget(Class<T> clazz, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter);

    interface IBoundsGetter {
        Rect apply(IClientUtils registry, ILootEntry entry, int x, int y);
    }
}
