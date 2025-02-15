package com.yanny.ali.api;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IClientUtils {
    Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<ILootEntry> entries, int x, int y,
                                                 List<ILootFunction> functions, List<ILootCondition> conditions);

    Rect getBounds(IClientUtils registry, List<ILootEntry> entries, int x, int y);

    @Nullable
    WidgetDirection getWidgetDirection(ILootEntry entry);
}
