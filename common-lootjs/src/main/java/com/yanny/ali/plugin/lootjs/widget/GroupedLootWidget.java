package com.yanny.ali.plugin.lootjs.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;

public class GroupedLootWidget extends ListWidget {
    public GroupedLootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    @Override
    public IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        return WidgetUtils.getRandomWidget(rect, entry);
    }
}
