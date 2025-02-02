package com.yanny.advanced_loot_info.api;

import com.mojang.datafixers.util.Pair;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;

import java.util.List;

public interface IClientRegistry {
    <T extends LootEntry> void registerWidget(Class<T> clazz, WidgetDirection direction, IWidgetFactory factory, IBoundsGetter boundsGetter);

    Pair<List<Widget>, Bounds> createWidgets(EmiRecipe recipe, IClientRegistry registry, List<LootEntry> entries, int x, int y, List<ILootFunction> functions, List<ILootCondition> conditions);

    Bounds getBounds(IClientRegistry registry, List<LootEntry> entries, int x, int y);

    interface IBoundsGetter {
        Bounds apply(IClientRegistry registry, LootEntry entry, int x, int y);
    }
}
