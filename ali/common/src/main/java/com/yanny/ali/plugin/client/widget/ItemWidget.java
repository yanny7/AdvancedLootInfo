package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.ItemNode;

public class ItemWidget implements IWidget {
    private final RelativeRect bounds;

    public ItemWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        ItemNode node = (ItemNode) entry;
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
