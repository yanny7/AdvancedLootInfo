package com.yanny.awi.rei.compatibility.rei;

import com.yanny.aci.api.Rect;
import com.yanny.aci.compatibility.AbstractScrollWidget;
import com.yanny.awi.plugin.client.WidgetUtils;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReiScrollWidget extends Widget {
    private static final int NO_MOUSE = -10000;

    private final AbstractScrollWidget scrollWidget;
    private final List<Widget> widgets;

    public ReiScrollWidget(Rect rect, int contentWidth, int contentHeight, List<Widget> widgets) {
        this.widgets = widgets;
        scrollWidget = new AbstractScrollWidget(rect, contentWidth, contentHeight) {
            @NotNull
            @Override
            protected ResourceLocation getTexture() {
                return WidgetUtils.TEXTURE_LOC;
            }

            @Override
            public void renderWidgets(GuiGraphics guiGraphics, double mouseX, double mouseY) {
                for (Widget widget : widgets) {
                    if (widget instanceof WidgetWithBounds boundedWidget) {
                        Rectangle b = boundedWidget.getBounds();

                        if (isOutsideViewport(b.x, b.y, b.width, b.height)) {
                            continue;
                        }
                    }

                    widget.render(guiGraphics, (int) mouseX, (int) mouseY, 0);
                }
            }
        };
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        // REI queues tooltips from render's mouse args, so off-screen coords are the only way to mute one we still draw.
        if (scrollWidget.isMouseOverContent(mouseX, mouseY)) {
            scrollWidget.render(guiGraphics, mouseX, mouseY);
        } else {
            scrollWidget.render(guiGraphics, NO_MOUSE, NO_MOUSE);
        }
    }

    @NotNull
    @Override
    public List<? extends GuiEventListener> children() {
        return widgets;
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        return super.mouseReleased(d + scrollWidget.getScrollAmountX(), e + scrollWidget.getScrollAmountY(), i);
    }

    @Override
    public void mouseMoved(double d, double e) {
        super.mouseMoved(d + scrollWidget.getScrollAmountX(), e + scrollWidget.getScrollAmountY());
    }

    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return super.containsMouse(mouseX + scrollWidget.getScrollAmountX(), mouseY + scrollWidget.getScrollAmountY());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrollWidget.onScrollbarClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX + scrollWidget.getScrollAmountX(), mouseY + scrollWidget.getScrollAmountY(), button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        if (scrollWidget.onMouseScrolled(mouseX, mouseY, scrollDeltaY)) {
            return true;
        }

        return super.mouseScrolled(mouseX + scrollWidget.getScrollAmountX(), mouseY + scrollWidget.getScrollAmountY(), scrollDeltaX, scrollDeltaY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrollWidget.onMouseDragged(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseDragged(mouseX + scrollWidget.getScrollAmountX(), mouseY + scrollWidget.getScrollAmountY(), button, dragX, dragY);
    }
}
