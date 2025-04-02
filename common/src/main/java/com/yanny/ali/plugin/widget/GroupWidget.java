package com.yanny.ali.plugin.widget;

import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.plugin.WidgetUtils;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class GroupWidget extends CompositeWidget {
    public GroupWidget(IWidgetUtils utils, LootPoolEntryContainer entry, int x, int y, int sumWeight,
                       List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        super(utils, entry, x, y, sumWeight, functions, conditions);
        widgets.add(WidgetUtils.getGroupWidget(x, y));
    }
}
