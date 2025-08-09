package com.yanny.ali.compatibility.rei;

import com.yanny.ali.api.IWidget;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(bounds.getX(), bounds.getY());
        widget.render(guiGraphics, mouseX, mouseY);
        guiGraphics.pose().popMatrix();
    }

    @Nullable
    public Tooltip getTooltip(Point point) {
        if (point.x >= bounds.getMinX() && point.x <= bounds.getMaxX() && point.y >= bounds.getMinY()) {
            return Tooltip.create(widget.getTooltipComponents(point.getX() - bounds.getX(), point.getY() - bounds.getY()));
        } else {
            return null;
        }
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
