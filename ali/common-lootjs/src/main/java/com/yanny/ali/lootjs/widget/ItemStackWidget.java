package com.yanny.ali.lootjs.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.plugin.client.WidgetUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class ItemStackWidget implements IWidget {
    private final RelativeRect bounds;
    private final IWidget widget;
    private final boolean modified;

    public ItemStackWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int ignoredMaxWidth) {
        ItemStackNode node = (ItemStackNode) entry;

        utils.addSlotWidget(node.getModifiedItem(), node, rect);
        bounds = rect;
        bounds.setDimensions(18, 18);
        widget = WidgetUtils.getUnknownWidget(rect);
        modified = node.isModified();
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
