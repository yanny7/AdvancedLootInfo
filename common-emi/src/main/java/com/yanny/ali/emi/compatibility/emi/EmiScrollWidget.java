package com.yanny.ali.emi.compatibility.emi;

import com.yanny.ali.api.Rect;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
import com.yanny.ali.emi.compatibility.IMouseEvents;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.LinkedList;
import java.util.List;

public class EmiScrollWidget extends Widget implements IMouseEvents {
    private final AbstractScrollWidget scrollWidget;
    private final List<Widget> widgets;
    private final Bounds bounds;

    public EmiScrollWidget(Rect rect, int contentHeight, List<Widget> widgets) {
        this.widgets = widgets;
        bounds = new Bounds(rect.x(), rect.y(), rect.width(), rect.height());
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
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollDeltaY) {
        return scrollWidget.onMouseScrolled(mouseX, mouseY, scrollDeltaY);
    }

    @Override
    public boolean onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return scrollWidget.onMouseDragged(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (scrollWidget.onScrollbarClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (bounds.contains(mouseX, mouseY)) {
            float scrollAmount = scrollWidget.getScrollAmount();

            for (Widget widget : widgets) {
                Bounds b = widget.getBounds();

                if (b.contains(mouseX, (int) (mouseY + scrollAmount))) {
                    if (widget.mouseClicked(mouseX, (int) (mouseY + scrollAmount), button)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        scrollWidget.render(draw, mouseX, mouseY);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        List<ClientTooltipComponent> components = new LinkedList<>();
        float scrollAmount = scrollWidget.getScrollAmount();

        for (Widget widget : widgets) {
            Bounds b = widget.getBounds();

            if (b.contains(mouseX, (int) (mouseY + scrollAmount))) {
                components.addAll(widget.getTooltip(mouseX, (int) (mouseY + scrollAmount)));
            }
        }

        return components;
    }
}
