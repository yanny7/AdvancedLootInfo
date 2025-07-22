package com.yanny.ali.api;

import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class ListWidget extends IWidget {
    public static final ResourceLocation TEXTURE_LOC = ResourceLocation.fromNamespaceAndPath("ali", "textures/gui/gui.png");

    public static final int GROUP_WIDGET_WIDTH = 7;
    public static final int GROUP_WIDGET_HEIGHT = 18;

    private final List<IWidget> widgets;
    private final RelativeRect bounds;

    public ListWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        this(utils, entry, rect, maxWidth, entry);
    }

    public ListWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth, IDataNode tooltipNode) {
        super(entry.getId());
        IWidget groupWidget = getLootGroupWidget(rect, tooltipNode);
        boolean hasGroupWidget = groupWidget != null;
        int xOffset = hasGroupWidget ? GROUP_WIDGET_WIDTH : 0;

        widgets = new ArrayList<>();
        bounds = rect;

        if (hasGroupWidget) {
            widgets.add(groupWidget);
        }

        if (entry instanceof ListNode listNode) {
            RelativeRect subRect = new RelativeRect(xOffset, 0, rect.width - GROUP_WIDGET_WIDTH, 0, rect);

            widgets.addAll(utils.createWidgets(utils, listNode.nodes(), subRect, maxWidth));
            bounds.setDimensions(subRect.width + GROUP_WIDGET_WIDTH, subRect.height);
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

        blitRepeating(guiGraphics, bounds.getX() + 3, top, 2, height, 0, 0, 2, 18);
        lastDirection = null;

        for (IWidget widget : widgets) {
            WidgetDirection direction = widget.getDirection();

            if ((direction == WidgetDirection.VERTICAL || (lastDirection != null && direction != lastDirection)) && widget.getRect().offsetY > 0) {
                blitRepeating(guiGraphics, bounds.getX() + 4, widget.getRect().getY() + 8, 3, 2, 2, 0, 18, 2);
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

    private static void blitRepeating(GuiGraphics guiGraphics, int pTargetX, int pTargetY, int pTargetWidth, int pTargetHeight, int pSourceX, int pSourceY, int pSourceWidth, int pSourceHeight) {
        int i = pTargetX;
        int j;

        for(IntIterator intiterator = slices(pTargetWidth, pSourceWidth); intiterator.hasNext(); i += j) {
            j = intiterator.nextInt();

            int k = (pSourceWidth - j) / 2;
            int l = pTargetY;
            int i1;

            for(IntIterator iterator = slices(pTargetHeight, pSourceHeight); iterator.hasNext(); l += i1) {
                i1 = iterator.nextInt();
                int j1 = (pSourceHeight - i1) / 2;
                guiGraphics.blit(ListWidget.TEXTURE_LOC, i, l, pSourceX + k, pSourceY + j1, j, i1);
            }
        }
    }

    @NotNull
    private static IntIterator slices(int p_282197_, int p_282161_) {
        int i = Mth.positiveCeilDiv(p_282197_, p_282161_);
        return new Divisor(p_282197_, i);
    }
}
