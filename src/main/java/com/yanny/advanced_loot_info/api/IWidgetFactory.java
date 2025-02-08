package com.yanny.advanced_loot_info.api;

import java.util.List;

@FunctionalInterface
public interface IWidgetFactory {
    IEntryWidget create(IWidgetUtils registry, ILootEntry entry, int x, int y, int totalWeight,
                       List<ILootFunction> functions, List<ILootCondition> conditions);
}
