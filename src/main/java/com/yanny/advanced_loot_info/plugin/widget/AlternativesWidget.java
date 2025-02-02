package com.yanny.advanced_loot_info.plugin.widget;

import com.yanny.advanced_loot_info.api.IClientRegistry;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.LootEntry;
import com.yanny.advanced_loot_info.plugin.WidgetUtils;
import dev.emi.emi.api.recipe.EmiRecipe;

import java.util.List;

public class AlternativesWidget extends CompositeWidget {
    public AlternativesWidget(EmiRecipe recipe, IClientRegistry registry, LootEntry entry, int x, int y, int sumWeight,
                              List<ILootFunction> functions, List<ILootCondition> conditions) {
        super(recipe, registry, entry, x, y, sumWeight, functions, conditions);
        widgets.add(WidgetUtils.getAlternativesWidget(x, y));
    }
}
