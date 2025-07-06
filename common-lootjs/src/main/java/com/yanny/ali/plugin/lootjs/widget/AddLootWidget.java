package com.yanny.ali.plugin.lootjs.widget;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.ListWidget;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.lootjs.node.AddLootNode;

public class AddLootWidget extends ListWidget {
    public AddLootWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        super(utils, entry, x, y, maxWidth);
    }

    @Override
    public IWidget getLootGroupWidget(int x, int y, IDataNode entry) {
        return switch (((AddLootNode) entry).getAddType()) {
            case DEFAULT -> WidgetUtils.getAllWidget(x, y, entry);
            case SEQUENCE -> WidgetUtils.getSequentialWidget(x, y, entry);
            case ALTERNATIVES -> WidgetUtils.getAlternativesWidget(x, y, entry);
        };
    }
}
