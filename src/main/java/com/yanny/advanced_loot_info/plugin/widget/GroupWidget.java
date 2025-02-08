package com.yanny.advanced_loot_info.plugin.widget;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootEntry;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.IWidgetUtils;
import com.yanny.advanced_loot_info.plugin.WidgetUtils;

import java.util.List;

public class GroupWidget extends CompositeWidget {
    public GroupWidget(IWidgetUtils utils, ILootEntry entry, int x, int y, int sumWeight,
                       List<ILootFunction> functions, List<ILootCondition> conditions) {
        super(utils, entry, x, y, sumWeight, functions, conditions);
        widgets.add(WidgetUtils.getGroupWidget(x, y));
    }
}
