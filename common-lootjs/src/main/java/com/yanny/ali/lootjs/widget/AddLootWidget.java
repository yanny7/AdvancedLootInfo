package com.yanny.ali.lootjs.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.lootjs.node.AddLootNode;

public class AddLootWidget extends ListWidget {
    public AddLootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    @Override
    public IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        return switch (((AddLootNode) entry).getAddType()) {
            case DEFAULT -> WidgetUtils.getAllWidget(rect, entry);
            case SEQUENCE -> WidgetUtils.getSequentialWidget(rect, entry);
            case ALTERNATIVES -> WidgetUtils.getAlternativesWidget(rect, entry);
        };
    }
}
