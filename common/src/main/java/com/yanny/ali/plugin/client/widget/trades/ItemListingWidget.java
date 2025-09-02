package com.yanny.ali.plugin.client.widget.trades;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.client.widget.ItemStackWidget;
import com.yanny.ali.plugin.client.widget.ItemWidget;
import com.yanny.ali.plugin.client.widget.TagWidget;
import com.yanny.ali.plugin.common.nodes.ItemStackNode;
import com.yanny.ali.plugin.common.nodes.TagNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ItemListingWidget implements IWidget {
    private final List<IWidget> widgets;
    private final RelativeRect bounds;

    public ItemListingWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        ListNode node = (ListNode) entry;

        widgets = new ArrayList<>();

        addWidget(utils, rect, node.nodes().get(0), 0, maxWidth);
        addWidget(utils, rect, node.nodes().get(1), 20, maxWidth);
        widgets.add(WidgetUtils.getArrowWidget(new RelativeRect(40, 0, 24, 18, rect), entry));
        addWidget(utils, rect, node.nodes().get(2), 66, maxWidth);

        bounds = rect;
        bounds.setDimensions(maxWidth, 18);
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
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        List<Component> components = new LinkedList<>();

        for (IWidget widget : widgets) {
            RelativeRect b = widget.getRect();

            if (b.contains(mouseX, mouseY)) {
                components.addAll(widget.getTooltipComponents(mouseX, mouseY));
            }
        }

        return components;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean clicked = false;

        for (IWidget widget : widgets) {
            RelativeRect b = widget.getRect();

            if (b.contains(mouseX, mouseY)) {
                clicked |= widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        return clicked;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (IWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY);
        }
    }

    private void addWidget(IWidgetUtils utils, RelativeRect rect, IDataNode node, int offsetX, int maxWidth) {
        if (node instanceof ItemStackNode itemStackNode) {
            widgets.add(new ItemStackWidget(utils, itemStackNode, new RelativeRect(offsetX, 0, 18, 18, rect), maxWidth));
        } else if (node instanceof TagNode tagNode) {
            widgets.add(new TagWidget(utils, tagNode, new RelativeRect(offsetX, 0, 18, 18, rect), maxWidth));
        } else {
            widgets.add(new ItemWidget(utils, node, new RelativeRect(offsetX, 0, 18, 18, rect), maxWidth));
        }
    }
}
