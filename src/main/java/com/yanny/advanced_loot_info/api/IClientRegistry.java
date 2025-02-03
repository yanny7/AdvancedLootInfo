package com.yanny.advanced_loot_info.api;

import dev.emi.emi.api.widget.Bounds;

public interface IClientRegistry {
    <T extends LootEntry> void registerWidget(Class<T> clazz, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter);

    interface IBoundsGetter {
        Bounds apply(IClientUtils registry, LootEntry entry, int x, int y);
    }
}
