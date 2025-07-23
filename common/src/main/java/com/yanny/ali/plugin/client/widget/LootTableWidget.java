package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;

public class LootTableWidget extends ListWidget {
    public LootTableWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    public LootTableWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth, IDataNode tooltipNode) {
        super(utils, entry, rect, maxWidth, tooltipNode);
    }

    @Override
    public IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        return WidgetUtils.getAllWidget(rect, entry);
    }
}
