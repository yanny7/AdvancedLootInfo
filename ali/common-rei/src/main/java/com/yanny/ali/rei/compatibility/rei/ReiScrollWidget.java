package com.yanny.ali.rei.compatibility.rei;

import com.yanny.aci.api.Rect;
import com.yanny.aci.compatibility.AbstractScrollWidget;
import com.yanny.ali.plugin.client.WidgetUtils;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReiScrollWidget extends Widget {
    private static final int NO_MOUSE = -10000;

    private final AbstractScrollWidget scrollWidget;
    private final Rect rect;
    private final List<Widget> widgets;

    public ReiScrollWidget(Rect rect, int contentWidth, int contentHeight, List<Widget> widgets) {
        this.rect = rect;
        this.widgets = widgets;
        scrollWidget = new AbstractScrollWidget(rect, contentWidth, contentHeight) {
            @NotNull
            @Override
            protected Identifier getTexture() {
                return WidgetUtils.TEXTURE_LOC;
            }

            @Override
            public void renderWidgets(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
                for (Widget widget : widgets) {
                    if (widget instanceof WidgetWithBounds boundedWidget) {
                        Rectangle b = boundedWidget.getBounds();

                        if (isOutsideViewport(b.x, b.y, b.width, b.height)) {
                            continue;
                        }
                    }

                    widget.render((GuiGraphics) guiGraphics, (int) mouseX, (int) mouseY, 0);
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
    public boolean mouseReleased(MouseButtonEvent event) {
        double x = event.x() + scrollWidget.getScrollAmountX();
        double y = event.y() + scrollWidget.getScrollAmountY();
        MouseButtonEvent newEvent = new MouseButtonEvent(x, y, event.buttonInfo());

        if (!super.mouseReleased(newEvent)) {
            return this.getChildAt(x, y).filter((guiEventListener) -> guiEventListener.mouseReleased(newEvent)).isPresent();
        }

        return false;
    }

    @Override
    public void mouseMoved(double d, double e) {
        super.mouseMoved(d + scrollWidget.getScrollAmountX(), e + scrollWidget.getScrollAmountY());
    }

    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return rect.contains((int) mouseX, (int) mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (scrollWidget.onScrollbarClicked(event.x(), event.y(), event.button())) {
            return true;
        }

        return super.mouseClicked(new MouseButtonEvent(event.x() + scrollWidget.getScrollAmountX(), event.y() + scrollWidget.getScrollAmountY(), event.buttonInfo()), bl);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        if (scrollWidget.onMouseScrolled(mouseX, mouseY, scrollDeltaY)) {
            return true;
        }

        return super.mouseScrolled(mouseX + scrollWidget.getScrollAmountX(), mouseY + scrollWidget.getScrollAmountY(), scrollDeltaX, scrollDeltaY);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double d, double e) {
        if (scrollWidget.onMouseDragged(event.x(), event.y(), event.button())) {
            return true;
        }

        return super.mouseDragged(new MouseButtonEvent(event.x() + scrollWidget.getScrollAmountX(), event.y() + scrollWidget.getScrollAmountY(), event.buttonInfo()), d, e);
    }
}
