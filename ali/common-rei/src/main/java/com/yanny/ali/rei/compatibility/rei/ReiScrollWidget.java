package com.yanny.ali.rei.compatibility.rei;

import com.yanny.ali.api.Rect;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReiScrollWidget extends Widget {
    private final AbstractScrollWidget scrollWidget;
    private final Rect rect;
    private final List<Widget> widgets;

    public ReiScrollWidget(Rect rect, int contentHeight, List<Widget> widgets) {
        this.rect = rect;
        this.widgets = widgets;
        scrollWidget = new AbstractScrollWidget(rect, contentHeight) {
            @Override
            public void renderWidgets(GuiGraphics guiGraphics, double mouseX, double mouseY) {
                for (Widget widget : widgets) {
                    widget.render(guiGraphics, (int) mouseX, (int) mouseY, 0);
                }
            }
        };
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        scrollWidget.render(guiGraphics, mouseX, mouseY);
    }

    @NotNull
    @Override
    public List<? extends GuiEventListener> children() {
        return widgets;
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        double f = e + scrollWidget.getScrollAmount();

        if (!super.mouseReleased(d, f, i)) {
            return this.getChildAt(d, f).filter((guiEventListener) -> guiEventListener.mouseReleased(d, f, i)).isPresent();
        }

        return false;
    }

    @Override
    public void mouseMoved(double d, double e) {
        super.mouseMoved(d, e + scrollWidget.getScrollAmount());
    }

    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return rect.contains((int) mouseX, (int) mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrollWidget.onScrollbarClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY + scrollWidget.getScrollAmount(), button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        if (scrollWidget.onMouseScrolled(mouseX, mouseY, scrollDeltaY)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY + scrollWidget.getScrollAmount(), scrollDeltaX, scrollDeltaY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrollWidget.onMouseDragged(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY + scrollWidget.getScrollAmount(), button, dragX, dragY);
    }
}
