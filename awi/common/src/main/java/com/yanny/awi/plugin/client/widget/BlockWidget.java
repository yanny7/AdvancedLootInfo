package com.yanny.awi.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.plugin.common.nodes.BlockNode;
import org.jetbrains.annotations.NotNull;

public class BlockWidget implements IWidget {
    private final RelativeRect bounds;

    public BlockWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int ignoredMaxWidth) {
        BlockNode node = (BlockNode) entry;
        utils.addSlotWidget(node.getBlock(), node, rect);
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
