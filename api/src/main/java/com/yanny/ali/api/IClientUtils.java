package com.yanny.ali.api;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IClientUtils {
    Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<ILootEntry> entries, int x, int y,
                                                 List<ILootFunction> functions, List<ILootCondition> conditions);

    <T extends ILootCondition> List<Component> getConditionTooltip(Class<T> clazz, IUtils utils, int pad, ILootCondition condition);
    <T extends ILootFunction> List<Component> getFunctionTooltip(Class<T> clazz, IUtils utils, int pad, ILootFunction function);
    <T extends ILootEntry> List<Component> getEntryTooltip(Class<T> clazz, IUtils utils, int pad, ILootEntry entry);

    Rect getBounds(IClientUtils registry, List<ILootEntry> entries, int x, int y);

    @Nullable
    WidgetDirection getWidgetDirection(ILootEntry entry);
}
