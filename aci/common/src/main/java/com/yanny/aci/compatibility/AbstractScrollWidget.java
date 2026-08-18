package com.yanny.aci.compatibility;

import com.mojang.blaze3d.platform.InputConstants;
import com.yanny.aci.api.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public abstract class AbstractScrollWidget {
    protected static final int SCROLLBAR_PADDING = 2;
    protected static final int SCROLLBAR_WIDTH = 8;
    protected static final int SCROLLBAR_OFFSET = SCROLLBAR_WIDTH + 1;
    protected static final int MIN_SCROLL_MARKER_SIZE = 14;
    protected static final int SCROLL_RATE = 8;
    /** Slack around the viewport, so widgets that draw outside their declared bounds (3D block models) are never clipped. */
    protected static final int CULLING_MARGIN = 32;

    /**
     * Absolute area the widget being rendered right now is clipped to, or null outside {@link #renderWidgets}. A
     * picture-in-picture render state carries its own scissor rectangle instead of honouring {@link GuiGraphics}'
     * scissor stack, so a block drawn through one has to be told where the viewport ends or it draws over the rest of
     * the screen. Kept as static state rather than plumbed through, because the widgets that need it are handed to the
     * recipe viewer before the scroll widget that owns them exists.
     */
    @Nullable
    private static ScreenRectangle currentViewport;

    protected final Rect rect;
    protected final Rect verticalScrollRect;
    protected final Rect horizontalScrollRect;

    private final int contentWidth;
    private final int contentHeight;
    private final boolean horizontalScrollbar;
    private double dragOriginX = -1;
    private double dragOriginY = -1;
    private float scrollOffsetX = 0; // 0 - left, 1 - right
    private float scrollOffsetY = 0; // 0 - top, 1 - bottom

    public AbstractScrollWidget(Rect rect, int contentWidth, int contentHeight) {
        this.rect = rect;
        this.contentWidth = contentWidth;
        this.contentHeight = contentHeight;
        this.horizontalScrollbar = contentWidth > getViewportWidth();
        this.verticalScrollRect = new Rect(rect.width() - SCROLLBAR_WIDTH, 0, SCROLLBAR_WIDTH, getViewportHeight());
        this.horizontalScrollRect = new Rect(0, rect.height() - SCROLLBAR_WIDTH, getViewportWidth(), SCROLLBAR_WIDTH);
    }

    public abstract void renderWidgets(GuiGraphics guiGraphics, double mouseX, double mouseY);

    /**
     * The atlas holding the scrollbar sprites. Every implementor's texture must place them at the same coordinates,
     * because the u/v offsets used by {@link #render} live here rather than in the individual mods.
     */
    @NotNull
    protected abstract Identifier getTexture();

    public void render(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawContents(guiGraphics, mouseX, mouseY);

        RenderingUtils.blitNineSliced(guiGraphics, getTexture(), verticalScrollRect.x(), verticalScrollRect.y(), verticalScrollRect.width(), verticalScrollRect.height(), 2, 2, 2, 2, 16, 16, 2, 2);

        Rect verticalMarker = calculateVerticalMarkerArea();
        RenderingUtils.blitNineSliced(guiGraphics, getTexture(), verticalMarker.x(), verticalMarker.y(), verticalMarker.width(), verticalMarker.height(), 2, 2, 2, 1, 12, 17, 18, 0);

        if (horizontalScrollbar) {
            RenderingUtils.blitNineSliced(guiGraphics, getTexture(), horizontalScrollRect.x(), horizontalScrollRect.y(), horizontalScrollRect.width(), horizontalScrollRect.height(), 2, 2, 2, 2, 16, 16, 2, 2);

            drawHorizontalMarker(guiGraphics, calculateHorizontalMarkerArea());
        }
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollDeltaY) {
        if (rect.contains((int) mouseX, (int) mouseY)) {
            if (Minecraft.getInstance().hasShiftDown() && horizontalScrollbar) {
                scrollOffsetX = applyScroll(scrollOffsetX, getHiddenAmountX(), scrollDeltaY);
            } else {
                scrollOffsetY = applyScroll(scrollOffsetY, getHiddenAmountY(), scrollDeltaY);
            }

            return true;
        }

        return false;
    }

    public boolean onMouseDragged(double mouseX, double mouseY, int button) {
        if (rect.contains((int) mouseX, (int) mouseY)) {
            if (button != InputConstants.MOUSE_BUTTON_LEFT) {
                return false;
            }

            if (dragOriginX >= 0) {
                moveHorizontalScrollbarTo(calculateHorizontalMarkerArea(), mouseX - dragOriginX);
                return true;
            }

            if (dragOriginY >= 0) {
                moveVerticalScrollbarTo(calculateVerticalMarkerArea(), mouseY - dragOriginY);
                return true;
            }
        }

        return false;
    }

    public boolean onScrollbarClicked(double mouseX, double mouseY, int button) {
        resetDrag();

        if (horizontalScrollbar && horizontalScrollRect.contains((int) mouseX, (int) mouseY)) {
            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                Rect markerArea = calculateHorizontalMarkerArea();

                if (!markerArea.contains((int) mouseX, (int) mouseY)) {
                    moveHorizontalScrollbarTo(markerArea, mouseX - (markerArea.width() / 2.0));
                    markerArea = calculateHorizontalMarkerArea();
                }

                dragOriginX = mouseX - markerArea.x();
            }

            return true;
        }

        if (verticalScrollRect.contains((int) mouseX, (int) mouseY)) {
            if (getHiddenAmountY() == 0) {
                return false;
            }

            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                Rect markerArea = calculateVerticalMarkerArea();

                if (!markerArea.contains((int) mouseX, (int) mouseY)) {
                    moveVerticalScrollbarTo(markerArea, mouseY - (markerArea.height() / 2.0));
                    markerArea = calculateVerticalMarkerArea();
                }

                dragOriginY = mouseY - markerArea.y();
            }

            return true;
        }

        return false;
    }

    public void resetDrag() {
        dragOriginX = -1;
        dragOriginY = -1;
    }

    /** Whether the raw (unscrolled) mouse is over the content viewport rather than over one of the scrollbars. */
    public boolean isMouseOverContent(double mouseX, double mouseY) {
        // A held drag keeps firing after the cursor leaves the bar, and the widget it lands on must not claim the mouse.
        if ((dragOriginX >= 0 || dragOriginY >= 0) && Minecraft.getInstance().mouseHandler.isLeftPressed()) {
            return false;
        }

        return mouseX >= rect.x() && mouseX < rect.x() + getViewportWidth()
                && mouseY >= rect.y() && mouseY < rect.y() + getViewportHeight();
    }

    public float getScrollAmountX() {
        return getHiddenAmountX() * scrollOffsetX;
    }

    public float getScrollAmountY() {
        return getHiddenAmountY() * scrollOffsetY;
    }

    /**
     * Tests whether a content-space span is scrolled completely out of view. Such widgets get skipped instead of
     * drawn - the scissor in {@link #drawContents} would discard them anyway, but only after they have already been
     * submitted as draw calls.
     */
    protected boolean isOutsideViewport(int x, int y, int width, int height) {
        float scrollAmountX = getScrollAmountX();
        float scrollAmountY = getScrollAmountY();

        if (y + height + CULLING_MARGIN < scrollAmountY || y - CULLING_MARGIN > scrollAmountY + getViewportHeight()) {
            return true;
        }

        return x + width + CULLING_MARGIN < scrollAmountX || x - CULLING_MARGIN > scrollAmountX + getViewportWidth();
    }

    protected int getHiddenAmountX() {
        return Math.max(contentWidth - getViewportWidth(), 0);
    }

    protected int getHiddenAmountY() {
        return Math.max(contentHeight - getViewportHeight(), 0);
    }

    private int getViewportWidth() {
        return rect.width() - SCROLLBAR_OFFSET;
    }

    private int getViewportHeight() {
        return horizontalScrollbar ? rect.height() - SCROLLBAR_OFFSET : rect.height();
    }

    private Rect calculateVerticalMarkerArea() {
        int totalSpace = verticalScrollRect.height() - 2;
        int markerWidth = verticalScrollRect.width() - 2;
        int markerHeight = Math.round(totalSpace * Math.min(1, getViewportHeight() / (float) contentHeight));

        markerHeight = Math.max(markerHeight, MIN_SCROLL_MARKER_SIZE);

        int markerY = Math.round((totalSpace - markerHeight) * scrollOffsetY);
        return new Rect(verticalScrollRect.x() + 1, verticalScrollRect.y() + 1 + markerY, markerWidth, markerHeight);
    }

    /** The atlas has no horizontal marker, and the sprite's stripes run across its length, so it is rotated. */
    private void drawHorizontalMarker(GuiGraphics guiGraphics, Rect markerArea) {
        Matrix3x2fStack poseStack = guiGraphics.pose();

        poseStack.pushMatrix();
        // A mirror would keep the lit bevel top-left, but GUI pipelines cull back faces, so only the rotation is applied.
        poseStack.translate(markerArea.x() + markerArea.width(), markerArea.y());
        poseStack.rotate(Mth.HALF_PI);

        try {
            RenderingUtils.blitNineSliced(guiGraphics, getTexture(), 0, 0, markerArea.height(), markerArea.width(), 2, 2, 2, 1, 12, 17, 18, 0);
        } finally {
            poseStack.popMatrix();
        }
    }

    private Rect calculateHorizontalMarkerArea() {
        int totalSpace = horizontalScrollRect.width() - 2;
        int markerHeight = horizontalScrollRect.height() - 2;
        int markerWidth = Math.round(totalSpace * Math.min(1, getViewportWidth() / (float) contentWidth));

        markerWidth = Math.max(markerWidth, MIN_SCROLL_MARKER_SIZE);

        int markerX = Math.round((totalSpace - markerWidth) * scrollOffsetX);
        return new Rect(horizontalScrollRect.x() + 1 + markerX, horizontalScrollRect.y() + 1, markerWidth, markerHeight);
    }

    private void moveVerticalScrollbarTo(Rect markerArea, double topY) {
        int minY = verticalScrollRect.y();
        int maxY = verticalScrollRect.y() + verticalScrollRect.height() - markerArea.height();

        scrollOffsetY = Mth.clamp((float) ((topY - minY) / (float) (maxY - minY)), 0.0F, 1.0F);
    }

    private void moveHorizontalScrollbarTo(Rect markerArea, double leftX) {
        int minX = horizontalScrollRect.x();
        int maxX = horizontalScrollRect.x() + horizontalScrollRect.width() - markerArea.width();

        scrollOffsetX = Mth.clamp((float) ((leftX - minX) / (float) (maxX - minX)), 0.0F, 1.0F);
    }

    private void drawContents(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        ScreenRectangle scissorArea = new ScreenRectangle(rect.x(), rect.y(), getViewportWidth(), getViewportHeight());
        ScreenRectangle previousViewport = currentViewport;
        float scrollAmountX = getScrollAmountX();
        float scrollAmountY = getScrollAmountY();

        currentViewport = scissorArea.transformAxisAligned(poseStack);
        guiGraphics.enableScissor(scissorArea.left(), scissorArea.top(), scissorArea.right(), scissorArea.bottom());
        poseStack.pushMatrix();
        poseStack.translateLocal(-scrollAmountX, -scrollAmountY);

        try {
            renderWidgets(guiGraphics, mouseX + scrollAmountX, mouseY + scrollAmountY);
        } finally {
            poseStack.popMatrix();
            guiGraphics.disableScissor();
            currentViewport = previousViewport;
        }
    }

    @Nullable
    public static ScreenRectangle getCurrentViewport() {
        return currentViewport;
    }

    private static float applyScroll(float scrollOffset, int hiddenAmount, double scrollDelta) {
        if (hiddenAmount <= 0) {
            return 0.0F;
        }

        return Mth.clamp(scrollOffset - (float) (scrollDelta * SCROLL_RATE / hiddenAmount), 0.0F, 1.0F);
    }

    public static int getScrollbarExtraWidth() {
        return SCROLLBAR_WIDTH + SCROLLBAR_PADDING;
    }
}
