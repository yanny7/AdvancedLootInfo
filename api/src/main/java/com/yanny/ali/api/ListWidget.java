package com.yanny.ali.api;

import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class ListWidget implements IWidget {
    public static final ResourceLocation TEXTURE_LOC = ResourceLocation.fromNamespaceAndPath("ali", "textures/gui/gui.png");

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
        List<IWidget> children = null;

        groupWidgetWidth = hasGroupWidget ? groupWidget.getRect().getWidth() : 0;
        bounds = rect;

        if (hasGroupWidget) {
            children = new ArrayList<>();
            children.add(groupWidget);
        }

        if (entry instanceof ListNode listNode) {
            RelativeRect subRect = new RelativeRect(groupWidgetWidth, 0, rect.getWidth() - groupWidgetWidth, 0, rect);
            List<IDataNode> nodes = listNode.nodes();

            if (!nodes.isEmpty()) {
                List<IWidget> widgetList = utils.createWidgets(utils, nodes, subRect, maxWidth);

                if (children != null) {
                    children.addAll(widgetList);
                } else {
                    children = new ArrayList<>(widgetList);
                }

                bounds.setDimensions(subRect.getWidth() + groupWidgetWidth, subRect.getHeight());
            } else {
                bounds.setDimensions(subRect.getWidth() + groupWidgetWidth, GROUP_WIDGET_HEIGHT);
            }
        } else {
            bounds.setDimensions(GROUP_WIDGET_WIDTH, GROUP_WIDGET_HEIGHT);
        }

        widgets = children != null ? children : Collections.emptyList();
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

        blitRepeating(guiGraphics, TEXTURE_LOC, bounds.getX() + groupWidgetWidth / 2, top, 2, height, 0, 0, 2, 18);
        lastDirection = null;

        for (IWidget widget : widgets) {
            WidgetDirection direction = widget.getDirection();

            if ((direction == WidgetDirection.VERTICAL || (lastDirection != null && direction != lastDirection)) && widget.getRect().getOffsetY() > 0) {
                blitRepeating(guiGraphics, TEXTURE_LOC, (int) (bounds.getX() + Math.floor((double) groupWidgetWidth / 2) + 1), widget.getRect().getY() + 8, (int) (Math.ceil((double) groupWidgetWidth / 2) - 1), 2, 2, 0, 18, 2);
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

    @Override
    public void onResize(RelativeRect parent, int maxWidth) {
        int posX = 0, posY = 0;
        WidgetDirection lastDirection = null;

        for (IWidget widget : widgets) {
            WidgetDirection direction = widget.getDirection();
            RelativeRect bounds = widget.getRect();

            if (bounds.getOffsetY() == 0) {
                widget.onResize(bounds, maxWidth);
                posX = bounds.getOffsetX() + bounds.getWidth();
                continue;
            }

            bounds.setOffset(posX, posY);

            if (lastDirection == null) {
                if (direction == WidgetDirection.HORIZONTAL) {
                    posX += bounds.getWidth();
                } else {
                    posY += bounds.getHeight() + PADDING;
                }
            } else {
                if (lastDirection == WidgetDirection.HORIZONTAL && direction == WidgetDirection.HORIZONTAL) {
                    if (bounds.getRight() <= maxWidth) {
                        posX += bounds.getWidth();
                    } else {
                        posX = bounds.getWidth();
                        posY += widgets.get(widgets.size() - 1).getRect().getHeight();
                        bounds.setOffset(0, posY);
                    }
                } else {
                    posX = 0;

                    if (direction != lastDirection) {
                        if (lastDirection == WidgetDirection.HORIZONTAL) {
                            posY += widgets.get(widgets.size() - 1).getRect().getHeight() + IWidget.PADDING;
                        }

                        bounds.setOffset(posX, posY);
                        widget.onResize(bounds, maxWidth);
                    }

                    if (direction != WidgetDirection.HORIZONTAL) {
                        posY += bounds.getHeight() + IWidget.PADDING;
                    } else {
                        posX += bounds.getWidth();
                    }
                }
            }

            lastDirection = direction;
        }

        int w = 0, h = 0;

        for (IWidget widget : widgets) {
            RelativeRect rect = widget.getRect();

            w = Math.max(w, rect.getOffsetX() + rect.getWidth());
            h = Math.max(h, rect.getOffsetY() + rect.getHeight());
        }

        parent.setDimensions(w, h);
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation pAtlasLocation, int pX, int pY, int pWidth, int pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, pAtlasLocation, pX, pY, pUOffset, pVOffset, pWidth, pHeight, pUWidth, pVHeight, 255, 255 );
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation pAtlasLocation, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight) {
        blit(guiGraphics, pAtlasLocation, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight);
    }

    public static void blitRepeating(GuiGraphics guiGraphics, ResourceLocation pAtlasLocation, int pTargetX, int pTargetY, int pTargetWidth, int pTargetHeight, int pSourceX, int pSourceY, int pSourceWidth, int pSourceHeight) {
        int i = pTargetX;
        int j;

        for(IntIterator intiterator = slices(pTargetWidth, pSourceWidth); intiterator.hasNext(); i += j) {
            j = intiterator.nextInt();
            int k = (pSourceWidth - j) / 2;
            int l = pTargetY;

            int i1;
            for(IntIterator intiterator1 = slices(pTargetHeight, pSourceHeight); intiterator1.hasNext(); l += i1) {
                i1 = intiterator1.nextInt();
                int j1 = (pSourceHeight - i1) / 2;
                blit(guiGraphics, pAtlasLocation, i, l, pSourceX + k, pSourceY + j1, j, i1);
            }
        }
    }

    @NotNull
    private static IntIterator slices(int p_282197_, int p_282161_) {
        int i = Mth.positiveCeilDiv(p_282197_, p_282161_);
        return new Divisor(p_282197_, i);
    }
}
