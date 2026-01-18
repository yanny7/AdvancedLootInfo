package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DynamicWidget implements IWidget {
    private final RelativeRect bounds;
    private final IWidget widget;

    public DynamicWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        widget = WidgetUtils.getDynamicWidget(rect, entry);
        bounds = widget.getRect();
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
        widget.render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return widget.getTooltipComponents(mouseX, mouseY);
    }
}
