package com.yanny.advanced_loot_info.api;

import dev.emi.emi.api.widget.Bounds;

public interface IClientRegistry {
    <T extends ILootEntry> void registerWidget(Class<T> clazz, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter);

    interface IBoundsGetter {
        Bounds apply(IClientUtils registry, ILootEntry entry, int x, int y);
    }
}
