package com.yanny.ali.compatibility.common;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.ali.api.Rect;
import com.yanny.ali.plugin.client.WidgetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractScrollWidget {
    protected static final int SCROLLBAR_PADDING = 2;
    protected static final int SCROLLBAR_WIDTH = 8;
    protected static final int MIN_SCROLL_MARKER_HEIGHT = 14;
    protected static final int SCROLL_RATE = 8;

    protected final Rect rect;
    protected final Rect contentRect;
    protected final Rect scrollRect;
    protected double dragOriginY = -1;
    protected float scrollOffsetY = 0; // 0 - top, 1 - bottom

    public AbstractScrollWidget(Rect rect, int contentHeight) {
        this.rect = rect;
        this.contentRect = new Rect(0, 0, rect.width() - getScrollbarExtraWidth(), contentHeight);
        this.scrollRect = calculateScrollArea(rect.width(), rect.height());
    }

    public abstract void renderWidgets(GuiGraphics guiGraphics, double mouseX, double mouseY);

    public void render(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        WidgetUtils.blitNineSliced(guiGraphics, WidgetUtils.TEXTURE_LOC, scrollRect.x(), scrollRect.y(), scrollRect.width(), scrollRect.height(), 2, 2, 2, 2, 16, 16, 2, 2);

        Rect markerArea = calculateScrollbarMarkerArea();
        WidgetUtils.blitNineSliced(guiGraphics, WidgetUtils.TEXTURE_LOC, markerArea.x(), markerArea.y(), markerArea.width(), markerArea.height(), 2, 2, 2, 1, 12, 17, 18, 0);

        drawContents(guiGraphics, mouseX, mouseY);
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollDeltaY) {
        if (rect.contains((int) mouseX, (int) mouseY)) {
            if (getHiddenAmount() > 0) {
                scrollOffsetY -= calculateScrollAmount(scrollDeltaY);
                scrollOffsetY = Mth.clamp(scrollOffsetY, 0.0F, 1.0F);
            } else {
                scrollOffsetY = 0.0f;
            }

            return true;
        }

        return false;
    }

    public boolean onMouseDragged(double mouseX, double mouseY, int button) {
        if (rect.contains((int) mouseX, (int) mouseY)) {
            if (dragOriginY < 0 || button != InputConstants.MOUSE_BUTTON_LEFT) {
                return false;
            }

            Rect scrollbarMarkerArea = calculateScrollbarMarkerArea();
            double topY = mouseY - dragOriginY;

            moveScrollbarTo(scrollbarMarkerArea, topY);
            return true;
        }

        return false;
    }

    public boolean onScrollbarClicked(double mouseX, double mouseY, int button) {
        if (scrollRect.contains((int) mouseX, (int) mouseY)) {
            if (getHiddenAmount() == 0) {
                return false;
            }

            if (button == 0) {
                Rect scrollMarkerArea = calculateScrollbarMarkerArea();

                if (!scrollMarkerArea.contains((int) mouseX, (int) mouseY)) {
                    moveScrollbarCenterTo(scrollMarkerArea, mouseY);
                    scrollMarkerArea = calculateScrollbarMarkerArea();
                }

                dragOriginY = mouseY - scrollMarkerArea.y();
            }

            return true;
        }

        return false;
    }

    public float getScrollAmount() {
        return getHiddenAmount() * scrollOffsetY;
    }

    protected Rect calculateScrollbarMarkerArea() {
        int totalSpace = scrollRect.height() - 2;
        int scrollMarkerWidth = scrollRect.width() - 2;
        int scrollMarkerHeight = Math.round(totalSpace * Math.min(1, rect.height() / (float) (contentRect.height())));

        scrollMarkerHeight = Math.max(scrollMarkerHeight, MIN_SCROLL_MARKER_HEIGHT);

        int scrollbarMarkerY = Math.round((totalSpace - scrollMarkerHeight) * scrollOffsetY);
        return new Rect(scrollRect.x() + 1, scrollRect.y() + 1 + scrollbarMarkerY, scrollMarkerWidth, scrollMarkerHeight);
    }

    protected void moveScrollbarCenterTo(Rect scrollMarkerArea, double centerY) {
        double topY = centerY - (scrollMarkerArea.height() / 2.0);
        moveScrollbarTo(scrollMarkerArea, topY);
    }

    protected int getHiddenAmount() {
        return Math.max(contentRect.height() - rect.height(), 0);
    }

    private void moveScrollbarTo(Rect scrollMarkerArea, double topY) {
        int minY = scrollRect.y();
        int maxY = scrollRect.y() + scrollRect.height() - scrollMarkerArea.height();
        double relativeY = topY - minY;
        int totalSpace = maxY - minY;
        scrollOffsetY = (float) (relativeY / (float) totalSpace);
        scrollOffsetY = Mth.clamp(scrollOffsetY, 0.0F, 1.0F);
    }

    private float calculateScrollAmount(double scrollDeltaY) {
        int totalHeight = Math.max(1, contentRect.height() - rect.height());
        double scrollAmount = scrollDeltaY * SCROLL_RATE;
        return (float) (scrollAmount / (double) totalHeight);
    }

    private void drawContents(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        PoseStack poseStack = guiGraphics.pose();
        ScreenRectangle scissorArea = new ScreenRectangle(rect.x(), rect.y(), rect.width(), rect.height());
        float scrollAmount = getScrollAmount();

        guiGraphics.enableScissor(scissorArea.left(), scissorArea.top(), scissorArea.right(), scissorArea.bottom());
        poseStack.pushPose();
        poseStack.translate(0.0, -scrollAmount, 0.0);

        try {
            renderWidgets(guiGraphics, mouseX, mouseY + scrollAmount);
        } finally {
            poseStack.popPose();
            guiGraphics.disableScissor();
        }
    }

    public static int getScrollbarExtraWidth() {
        return SCROLLBAR_WIDTH + SCROLLBAR_PADDING;
    }

    @NotNull
    private static Rect calculateScrollArea(int width, int height) {
        return new Rect(width - SCROLLBAR_WIDTH, 0, SCROLLBAR_WIDTH, height);
    }
}
