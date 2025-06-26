package com.yanny.ali.api;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class ListWidget extends IWidget {
    public static final ResourceLocation TEXTURE_LOC = new ResourceLocation("ali", "textures/gui/gui.png");

    public static final int GROUP_WIDGET_WIDTH = 7;
    public static final int GROUP_WIDGET_HEIGHT = 18;

    private final List<IWidget> widgets;
    private final Rect bounds;
    private final IClientUtils utils;

    public ListWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        this(utils, entry, x, y, maxWidth, entry);
    }

    public ListWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth, IDataNode tooltipNode) {
        super(entry.getId());
        IWidget groupWidget = getLootGroupWidget(x, y, tooltipNode);
        boolean hasGroupWidget = groupWidget != null;
        int xOffset = hasGroupWidget ? GROUP_WIDGET_WIDTH : 0;

        widgets = new ArrayList<>();
        this.utils = utils;

        if (hasGroupWidget) {
            widgets.add(groupWidget);
        }

        if (entry instanceof ListNode listNode) {
            Pair<List<IWidget>, Rect> info = utils.createWidgets(utils, listNode.nodes(), x + xOffset, y, maxWidth);

            widgets.addAll(new LinkedList<>(info.getFirst()));
            bounds = new Rect(x, y, info.getSecond().width() + xOffset, info.getSecond().height());
        } else {
            bounds = new Rect(x, y, GROUP_WIDGET_WIDTH, 18);
        }
    }

    @Nullable
    public abstract IWidget getLootGroupWidget(int x, int y, IDataNode entry);

    @Override
    public Rect getRect() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int lastY = 0;
        WidgetDirection lastDirection = null;

        for (IWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY);

            WidgetDirection direction = utils.getWidgetDirection(widget.getNodeId());

            if (direction == WidgetDirection.VERTICAL || (lastDirection != null && direction != lastDirection)) {
                lastY = Math.max(lastY, widget.getRect().y());
            }

            lastDirection = direction;
        }

        int top = bounds.y() + 18;
        int height = lastY - bounds.y() - 9;

        guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 3, top, 2, height, 0, 0, 2, 18);
        lastDirection = null;

        for (IWidget widget : widgets) {
            WidgetDirection direction = utils.getWidgetDirection(widget.getNodeId());

            if ((direction == WidgetDirection.VERTICAL || (lastDirection != null && direction != lastDirection)) && widget.getRect().y() > bounds.y() + 18) {
                guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 4, widget.getRect().y() + 8, 3, 2, 2, 0, 18, 2);
            }

            lastDirection = direction;
        }
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        List<Component> components = new LinkedList<>();

        for (IWidget widget : widgets) {
            Rect b = widget.getRect();

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
            Rect b = widget.getRect();

            if (b.contains(mouseX, mouseY)) {
                clicked |= widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        return clicked;
    }

    @NotNull
    public static Rect getBounds(IClientUtils registry, IDataNode entry, int x, int y, int maxWidth) {
        if (entry instanceof ListNode listNode) {
            Rect rect = registry.getBounds(registry, listNode.nodes(), x + GROUP_WIDGET_WIDTH, y, maxWidth);
            return new Rect(x, y, rect.width() + GROUP_WIDGET_WIDTH, rect.height());
        } else {
            return new Rect(x, y, GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        }
    }
}
