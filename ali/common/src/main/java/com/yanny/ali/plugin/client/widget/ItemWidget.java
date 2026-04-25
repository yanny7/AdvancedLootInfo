package com.yanny.ali.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import org.jetbrains.annotations.NotNull;

public class ItemWidget implements IWidget {
    private final RelativeRect bounds;

    public ItemWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int ignoredMaxWidth) {
        ItemNode node = (ItemNode) entry;
        utils.addSlotWidget(node.getModifiedItem(), node, rect);
        bounds = rect;
        bounds.setDimensions(18, 18);
    }

    @NotNull
    @Override
    public RelativeRect getRect() {
        return bounds;
    }

    @NotNull
    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.HORIZONTAL;
    }
}
