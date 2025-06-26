package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.ListWidget;
import com.yanny.ali.plugin.client.WidgetUtils;

public class LootTableWidget extends ListWidget {
    public LootTableWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        super(utils, entry, x, y, maxWidth);
    }

    public LootTableWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth, IDataNode tooltipNode) {
        super(utils, entry, x, y, maxWidth, tooltipNode);
    }

    @Override
    public IWidget getLootGroupWidget(int x, int y, IDataNode entry) {
        return WidgetUtils.getAllWidget(x, y, entry);
    }
}
