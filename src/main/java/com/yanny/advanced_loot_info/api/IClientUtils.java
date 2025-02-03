package com.yanny.advanced_loot_info.api;

import com.mojang.datafixers.util.Pair;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IClientUtils {
    Pair<List<EntryWidget>, Bounds> createWidgets(EmiRecipe recipe, IClientUtils registry, List<LootEntry> entries, int x, int y, List<ILootFunction> functions, List<ILootCondition> conditions);

    Bounds getBounds(IClientUtils registry, List<LootEntry> entries, int x, int y);

    @Nullable
    WidgetDirection getWidgetDirection(LootEntry entry);
}
