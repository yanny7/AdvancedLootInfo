package com.yanny.advanced_loot_info.compatibility.emi;

import com.yanny.advanced_loot_info.api.IWidget;
import com.yanny.advanced_loot_info.api.Rect;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;

public class WidgetWrapper extends Widget {
    private final IWidget widget;
    private final Bounds bounds;

    public WidgetWrapper(IWidget widget) {
        Rect rect = widget.getRect();

        this.widget = widget;
        bounds = new Bounds(rect.x(), rect.y(), rect.width(), rect.height());
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

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return widget.mouseClicked(mouseX, mouseY, button);
    }
}
