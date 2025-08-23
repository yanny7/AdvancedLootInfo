package com.yanny.ali.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class ListWidget implements IWidget {
    public static final ResourceLocation TEXTURE_LOC = new ResourceLocation("ali", "textures/gui/gui.png");

    public static final int GROUP_WIDGET_WIDTH = 7;
    public static final int GROUP_WIDGET_HEIGHT = 18;

    private final List<IWidget> widgets;
    private final RelativeRect bounds;
    private final int groupWidgetWidth;

    public ListWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        this(utils, entry, rect, maxWidth, entry);
    }

    public ListWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth, IDataNode tooltipNode) {
        IWidget groupWidget = getLootGroupWidget(rect, tooltipNode);
        boolean hasGroupWidget = groupWidget != null;

        groupWidgetWidth = hasGroupWidget ? groupWidget.getRect().width : 0;
        widgets = new ArrayList<>();
        bounds = rect;

        if (hasGroupWidget) {
            widgets.add(groupWidget);
        }

        if (entry instanceof ListNode listNode) {
            RelativeRect subRect = new RelativeRect(groupWidgetWidth, 0, rect.width - groupWidgetWidth, 0, rect);

            widgets.addAll(utils.createWidgets(utils, listNode.nodes(), subRect, maxWidth));
            bounds.setDimensions(subRect.width + groupWidgetWidth, subRect.height);
        } else {
            bounds.setDimensions(GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        }
    }

    @Nullable
    public abstract IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry);

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
        int lastY = 0;
        WidgetDirection lastDirection = null;

        for (IWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY);

            WidgetDirection direction = widget.getDirection();

            if (direction == WidgetDirection.VERTICAL || (lastDirection != null && direction != lastDirection)) {
                lastY = Math.max(lastY, widget.getRect().getY());
            }

            lastDirection = direction;
        }

        int top = bounds.getY() + 18;
        int height = lastY - bounds.getY() - 9;

        guiGraphics.blitRepeating(TEXTURE_LOC, bounds.getX() + groupWidgetWidth / 2, top, 2, height, 0, 0, 2, 18);
        lastDirection = null;

        for (IWidget widget : widgets) {
            WidgetDirection direction = widget.getDirection();

            if ((direction == WidgetDirection.VERTICAL || (lastDirection != null && direction != lastDirection)) && widget.getRect().offsetY > 0) {
                guiGraphics.blitRepeating(TEXTURE_LOC, (int) (bounds.getX() + Math.floor((double) groupWidgetWidth / 2) + 1), widget.getRect().getY() + 8, (int) (Math.ceil((double) groupWidgetWidth / 2) - 1), 2, 2, 0, 18, 2);
            }

            lastDirection = direction;
        }
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
}
