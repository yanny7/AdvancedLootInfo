package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.ReferenceNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ReferenceWidget implements IWidget {
    private final RelativeRect bounds;
    private final IWidget widget;

    public ReferenceWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        if (entry instanceof ListNode listNode && !listNode.nodes().isEmpty()) {
            widget = new LootTableWidget(utils, ((ReferenceNode) entry).nodes().get(0), rect, maxWidth, entry);
        } else {
            widget = new MissingWidget(rect);
        }

        bounds = widget.getRect();
    }

    @Override
    public RelativeRect getRect() {
        return bounds;
    }

    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.VERTICAL;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        widget.render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return widget.getTooltipComponents(mouseX, mouseY);
    }

    @Override
    public void onResize(RelativeRect parent, int maxWidth) {
        widget.onResize(bounds, maxWidth);
    }
}
