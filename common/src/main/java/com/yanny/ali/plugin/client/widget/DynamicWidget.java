package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;
import java.util.List;

public class DynamicWidget extends IWidget {
    private final List<Component> components;
    private final RelativeRect bounds;
    private final IWidget widget;

    public DynamicWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(entry.getId());
        widget = WidgetUtils.getDynamicWidget(rect, entry);
        bounds = widget.getRect();
        components = NodeUtils.toComponents(entry.getTooltip(), 0);
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
        List<Component> components = new LinkedList<>(widget.getTooltipComponents(mouseX, mouseY));

        components.addAll(this.components);
        return components;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return widget.mouseClicked(mouseX, mouseY, button);
    }
}
