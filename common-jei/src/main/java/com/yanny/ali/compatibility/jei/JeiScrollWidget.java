package com.yanny.ali.compatibility.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.ali.api.Rect;
import com.yanny.ali.plugin.client.WidgetUtils;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class JeiScrollWidget implements IRecipeWidget, IJeiInputHandler, ISlottedRecipeWidget {
    private static final int SCROLLBAR_PADDING = 2;
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int MIN_SCROLL_MARKER_HEIGHT = 14;
    private static final int SCROLL_RATE = 8;

    private final Rect rect;
    private final Rect contentRect;
    private final Rect scrollRect;
    private final List<IRecipeWidget> widgets;
    private double dragOriginY = -1;
    private float scrollOffsetY = 0; // 0 - top, 1 - bottom

    public JeiScrollWidget(Rect rect, int contentHeight, List<IRecipeWidget> widgets) {
        this.rect = rect;
        this.widgets = widgets;
        this.contentRect = new Rect(0, 0, rect.width() - getScrollBoxScrollbarExtraWidth(), contentHeight);
        this.scrollRect = calculateScrollArea(rect.width(), rect.height());
    }

    @NotNull
    @Override
    public ScreenRectangle getArea() {
        return new ScreenRectangle(rect.x(), rect.y(), rect.width(), rect.height());
    }

    @NotNull
    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(rect.x(), rect.y());
    }

    @NotNull
    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
        float scrollAmount = getHiddenAmount() * scrollOffsetY;

        for (IRecipeWidget widget : widgets) {
            if (widget instanceof ISlottedRecipeWidget slottedWidget) {
                Optional<RecipeSlotUnderMouse> slotUnderMouse = slottedWidget.getSlotUnderMouse(mouseX, mouseY + scrollAmount);

                if (slotUnderMouse.isPresent()) {
                    return Optional.of(new RecipeSlotUnderMouse(slotUnderMouse.get().slot(), 1, (int) (1 - scrollAmount)));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        WidgetUtils.blitNineSliced(guiGraphics, WidgetUtils.TEXTURE_LOC, scrollRect.x(), scrollRect.y(), scrollRect.width(), scrollRect.height(), 2, 2, 2, 2, 16, 16, 2, 2);

        Rect markerArea = calculateScrollbarMarkerArea();
        WidgetUtils.blitNineSliced(guiGraphics, WidgetUtils.TEXTURE_LOC, markerArea.x(), markerArea.y(), markerArea.width(), markerArea.height(), 2, 2, 2, 1, 12, 17, 18, 0);

        drawContents(guiGraphics, mouseX, mouseY, scrollOffsetY);
    }

    protected Rect calculateScrollbarMarkerArea() {
        int totalSpace = scrollRect.height() - 2;
        int scrollMarkerWidth = scrollRect.width() - 2;
        int scrollMarkerHeight = Math.round(totalSpace * Math.min(1, rect.height() / (float) (contentRect.height())));

        scrollMarkerHeight = Math.max(scrollMarkerHeight, MIN_SCROLL_MARKER_HEIGHT);

        int scrollbarMarkerY = Math.round((totalSpace - scrollMarkerHeight) * scrollOffsetY);
        return new Rect(scrollRect.x() + 1, scrollRect.y() + 1 + scrollbarMarkerY, scrollMarkerWidth, scrollMarkerHeight);
    }

    @Override
    public final boolean handleInput(double mouseX, double mouseY, IJeiUserInput userInput) {
        if (!userInput.isSimulate()) {
            dragOriginY = -1;
        }

        if (scrollRect.contains((int) mouseX, (int) mouseY)) {
            if (getHiddenAmount() == 0) {
                return false;
            }

            if (userInput.isSimulate()) {
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

    @Override
    public final boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        if (getHiddenAmount() > 0) {
            scrollOffsetY -= calculateScrollAmount(scrollDeltaY);
            scrollOffsetY = Mth.clamp(scrollOffsetY, 0.0F, 1.0F);
        } else {
            scrollOffsetY = 0.0f;
        }
        return true;
    }

    @Override
    public final boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
        if (dragOriginY < 0 || mouseKey.getValue() != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }

        Rect scrollbarMarkerArea = calculateScrollbarMarkerArea();
        double topY = mouseY - dragOriginY;

        moveScrollbarTo(scrollbarMarkerArea, topY);
        return true;
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        float scrollAmount = getHiddenAmount() * scrollOffsetY;
        widgets.forEach((widget) -> widget.getTooltip(tooltip, mouseX, mouseY + scrollAmount));
    }

    private void drawContents(GuiGraphics guiGraphics, double mouseX, double mouseY, float scrollOffsetY) {
        PoseStack poseStack = guiGraphics.pose();
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        ScreenRectangle scissorArea = transform(rect, pose);
        float scrollAmount = getHiddenAmount() * scrollOffsetY;

        guiGraphics.enableScissor(scissorArea.left(), scissorArea.top(), scissorArea.right(), scissorArea.bottom());
        poseStack.pushPose();
        poseStack.translate(0.0, -scrollAmount, 0.0);

        try {
            widgets.forEach((widget) -> widget.drawWidget(guiGraphics, mouseX, mouseY + scrollAmount));
        } finally {
            poseStack.popPose();
            guiGraphics.disableScissor();
        }
    }

    private void moveScrollbarCenterTo(Rect scrollMarkerArea, double centerY) {
        double topY = centerY - (scrollMarkerArea.height() / 2.0);
        moveScrollbarTo(scrollMarkerArea, topY);
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

    private int getHiddenAmount() {
        return Math.max(contentRect.height() - rect.height(), 0);
    }

    @NotNull
    public static ScreenRectangle transform(Rect rect, Matrix4f pose) {
        Vector3f topLeft = new Vector3f(rect.x(), rect.y(), 1.0f);
        Vector3f bottomRight = new Vector3f(rect.x() + rect.width(), rect.y() + rect.height(), 1.0f);

        topLeft = pose.transformPosition(topLeft);
        bottomRight = pose.transformPosition(bottomRight);

        int x = Math.round(topLeft.x);
        int y = Math.round(topLeft.y);
        return new ScreenRectangle(x, y, Math.round(bottomRight.x) - x, Math.round(bottomRight.y) - y);
    }

    @NotNull
    private static Rect calculateScrollArea(int width, int height) {
        return new Rect(width - SCROLLBAR_WIDTH, 0, SCROLLBAR_WIDTH, height);
    }

    public static int getScrollBoxScrollbarExtraWidth() {
        return SCROLLBAR_WIDTH + SCROLLBAR_PADDING;
    }
}
