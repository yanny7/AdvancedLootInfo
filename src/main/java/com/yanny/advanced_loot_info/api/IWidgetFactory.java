package com.yanny.advanced_loot_info.api;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Widget;

import java.util.List;

@FunctionalInterface
public interface IWidgetFactory {
    Widget create(EmiRecipe recipe, IClientRegistry registry, LootEntry entry, int x, int y, int totalWeight,
                  List<ILootFunction> functions, List<ILootCondition> conditions);
}
