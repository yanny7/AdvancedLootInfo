package com.yanny.ali.emi.compatibility.emi;

import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.RelativeRect;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;

public class EmiWidgetWrapper extends Widget {
    private final IWidget widget;
    private final Bounds bounds;

    public EmiWidgetWrapper(IWidget widget) {
        RelativeRect rect = widget.getRect();

        this.widget = widget;
        bounds = new Bounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float v) {
        widget.render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        return widget.getTooltipComponents(mouseX, mouseY).stream().map((t) -> ClientTooltipComponent.create(t.getVisualOrderText())).toList();
    }
}
