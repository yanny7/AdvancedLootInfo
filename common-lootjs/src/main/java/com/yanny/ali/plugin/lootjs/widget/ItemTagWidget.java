package com.yanny.ali.plugin.lootjs.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.lootjs.node.ItemTagNode;

public class ItemTagWidget extends IWidget {
    private final RelativeRect bounds;

    public ItemTagWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(entry.getId());
        ItemTagNode node = (ItemTagNode) entry;

        utils.addSlotWidget(node.getModifiedItem(), node, rect);
        bounds = rect;
        bounds.setDimensions(18, 18);
    }

    @Override
    public RelativeRect getRect() {
        return bounds;
    }

    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.HORIZONTAL;
    }
}
