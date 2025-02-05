package com.yanny.advanced_loot_info.api;

import dev.emi.emi.api.recipe.EmiRecipe;

import java.util.List;

@FunctionalInterface
public interface IWidgetFactory {
    EntryWidget create(EmiRecipe recipe, IClientUtils registry, ILootEntry entry, int x, int y, int totalWeight,
                       List<ILootFunction> functions, List<ILootCondition> conditions);
}
