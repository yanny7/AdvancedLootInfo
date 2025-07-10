package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.TagNode;

public class TagWidget extends IWidget {
    private final RelativeRect bounds;

    public TagWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(entry.getId());
        TagNode tagEntry = (TagNode) entry;

        utils.addSlotWidget(tagEntry.getModifiedItem(), tagEntry, rect);
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
