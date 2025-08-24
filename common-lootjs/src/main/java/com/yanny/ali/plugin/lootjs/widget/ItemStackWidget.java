package com.yanny.ali.plugin.lootjs.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.lootjs.node.ItemStackNode;
import net.minecraft.client.gui.GuiGraphics;

public class ItemStackWidget implements IWidget {
    private final RelativeRect bounds;
    private final IWidget widget;
    private final boolean modified;

    public ItemStackWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        ItemStackNode node = (ItemStackNode) entry;

        utils.addSlotWidget(node.getModifiedItem(), node, rect);
        bounds = rect;
        bounds.setDimensions(18, 18);
        widget = WidgetUtils.getUnknownWidget(rect, entry);
        modified = node.isModified();
    }

    @Override
    public RelativeRect getRect() {
        return bounds;
    }

    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.HORIZONTAL;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (modified) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            widget.render(guiGraphics, mouseX, mouseY);
            guiGraphics.pose().popPose();
        }
    }
}
