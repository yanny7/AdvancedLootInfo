package com.yanny.ali.plugin.lootjs.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.lootjs.node.LootEntryNode;

public class LootEntryWidget extends ListWidget {
    public LootEntryWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
    }

    @Override
    public IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        if (entry instanceof LootEntryNode entryNode) {
            return entryNode.isRandom() ? WidgetUtils.getRandomWidget(rect, entry) : null;
        }

        return null;
    }
}
