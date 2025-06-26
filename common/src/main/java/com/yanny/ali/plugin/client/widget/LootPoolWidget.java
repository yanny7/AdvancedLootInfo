package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.ListWidget;
import com.yanny.ali.plugin.client.WidgetUtils;

public class LootPoolWidget extends ListWidget {
    public LootPoolWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        super(utils, entry, x, y, maxWidth);
    }

    @Override
    public IWidget getLootGroupWidget(int x, int y, IDataNode entry) {
        return WidgetUtils.getRandomWidget(x, y, entry);
    }
}
