package com.yanny.awi.emi.compatibility.emi;

import com.yanny.aci.api.Rect;
import com.yanny.aci.compatibility.AbstractScrollWidget;
import com.yanny.awi.plugin.client.WidgetUtils;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class EmiScrollWidget extends Widget implements IMouseEvents {
    private final AbstractScrollWidget scrollWidget;
    private final List<Widget> widgets;
    private final Bounds bounds;

    public EmiScrollWidget(Rect rect, int contentWidth, int contentHeight, List<Widget> widgets) {
        this.widgets = widgets;
        bounds = new Bounds(rect.x(), rect.y(), rect.width(), rect.height());
        scrollWidget = new AbstractScrollWidget(rect, contentWidth, contentHeight) {
            @NotNull
            @Override
            protected ResourceLocation getTexture() {
                return WidgetUtils.TEXTURE_LOC;
            }

            @Override
            public void renderWidgets(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
                for (Widget widget : widgets) {
                    Bounds b = widget.getBounds();

                    if (isOutsideViewport(b.x(), b.y(), b.width(), b.height())) {
                        continue;
                    }

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

        if (scrollWidget.isMouseOverContent(mouseX, mouseY)) {
            int contentX = (int) (mouseX + scrollWidget.getScrollAmountX());
            int contentY = (int) (mouseY + scrollWidget.getScrollAmountY());

            for (Widget widget : widgets) {
                Bounds b = widget.getBounds();

                if (b.contains(contentX, contentY)) {
                    if (widget.mouseClicked(contentX, contentY, button)) {
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
    public void render(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta) {
        scrollWidget.render(draw, mouseX, mouseY);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        List<ClientTooltipComponent> components = new LinkedList<>();

        if (!scrollWidget.isMouseOverContent(mouseX, mouseY)) {
            return components;
        }

        int contentX = (int) (mouseX + scrollWidget.getScrollAmountX());
        int contentY = (int) (mouseY + scrollWidget.getScrollAmountY());

        for (Widget widget : widgets) {
            Bounds b = widget.getBounds();

            if (b.contains(contentX, contentY)) {
                components.addAll(widget.getTooltip(contentX, contentY));
            }
        }

        return components;
    }
}
