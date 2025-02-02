package com.yanny.advanced_loot_info.plugin.widget;

import com.yanny.advanced_loot_info.api.IClientRegistry;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.LootEntry;
import com.yanny.advanced_loot_info.plugin.WidgetUtils;
import com.yanny.advanced_loot_info.plugin.entry.CompositeEntry;
import dev.emi.emi.api.recipe.EmiRecipe;

import java.util.List;

public class GroupWidget extends CompositeWidget {
    public GroupWidget(EmiRecipe recipe, IClientRegistry registry, LootEntry entry, int x, int y, int sumWeight,
                       List<ILootFunction> functions, List<ILootCondition> conditions) {
        super(recipe, registry, (CompositeEntry) entry, x, y, sumWeight, functions, conditions);
        widgets.add(WidgetUtils.getGroupWidget(x, y));
    }
}
