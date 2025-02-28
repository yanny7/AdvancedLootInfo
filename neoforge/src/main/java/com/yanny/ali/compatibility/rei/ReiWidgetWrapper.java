package com.yanny.ali.compatibility.rei;

import com.yanny.ali.api.IWidget;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReiWidgetWrapper extends WidgetWithBounds {
    private final IWidget widget;
    private final Rectangle bounds;

    public ReiWidgetWrapper(IWidget widget, Rectangle bounds) {
        this.widget = widget;
        this.bounds = bounds;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(bounds.getX(), bounds.getY(), 0);
        widget.render(guiGraphics, mouseX, mouseY);
        guiGraphics.pose().popPose();
    }

    public Tooltip getTooltip(Point point) {
        return Tooltip.create(widget.getTooltipComponents(point.getX() - bounds.getX(), point.getY() - bounds.getY()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return widget.mouseClicked((int) mouseX, (int) mouseY, button);
    }

    @NotNull
    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }
}
