package com.yanny.advanced_loot_info.api;

public interface IClientRegistry {
    <T extends ILootEntry> void registerWidget(Class<T> clazz, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter);

    interface IBoundsGetter {
        Rect apply(IClientUtils registry, ILootEntry entry, int x, int y);
    }
}
