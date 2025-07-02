package com.yanny.ali.plugin.kubejs.widget;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.ListWidget;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.kubejs.node.LootEntryNode;

public class LootEntryWidget extends ListWidget {
    public LootEntryWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        super(utils, entry, x, y, maxWidth);
    }

    @Override
    public IWidget getLootGroupWidget(int x, int y, IDataNode entry) {
        if (entry instanceof LootEntryNode entryNode) {
            return entryNode.isRandom() ? WidgetUtils.getRandomWidget(x, y, entry) : null;
        }

        return null;
    }
}
