package com.yanny.ali.plugin.widget;

import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.ILootEntry;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.plugin.WidgetUtils;

import java.util.List;

public class AlternativesWidget extends CompositeWidget {
    public AlternativesWidget(IWidgetUtils utils, ILootEntry entry, int x, int y, int sumWeight,
                              List<ILootFunction> functions, List<ILootCondition> conditions) {
        super(utils, entry, x, y, sumWeight, functions, conditions);
        widgets.add(WidgetUtils.getAlternativesWidget(x, y));
    }
}
