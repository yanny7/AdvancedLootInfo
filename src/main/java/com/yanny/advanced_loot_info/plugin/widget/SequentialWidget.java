package com.yanny.advanced_loot_info.plugin.widget;

import com.yanny.advanced_loot_info.api.IClientUtils;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.LootEntry;
import com.yanny.advanced_loot_info.plugin.WidgetUtils;
import dev.emi.emi.api.recipe.EmiRecipe;

import java.util.List;

public class SequentialWidget extends CompositeWidget {
    public SequentialWidget(EmiRecipe recipe, IClientUtils utils, LootEntry entry, int x, int y, int sumWeight,
                            List<ILootFunction> functions, List<ILootCondition> conditions) {
        super(recipe, utils, entry, x, y, sumWeight, functions, conditions);
        widgets.add(WidgetUtils.getSequentialWidget(x, y));
    }
}
