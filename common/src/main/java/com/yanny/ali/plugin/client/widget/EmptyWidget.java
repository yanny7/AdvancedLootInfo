package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class EmptyWidget implements IWidget {
    private final RelativeRect bounds;
    private final IWidget widget;

    public EmptyWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        bounds = rect;
        bounds.setDimensions(18, 18);
        widget = WidgetUtils.getEmptyWidget(rect, entry);
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
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return widget.getTooltipComponents(mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        widget.render(guiGraphics, mouseX, mouseY);
    }
}
