package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.Rect;
import com.yanny.ali.api.RelativeRect;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JeiWidgetWrapper implements IRecipeWidget {
    private final IWidget widget;
    private final ScreenPosition position;
    private final RelativeRect rect;

    public JeiWidgetWrapper(IWidget widget) {
        this.rect = widget.getRect();
        this.widget = widget;
        position = new ScreenPosition(0, 0);
    }

    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        widget.render(guiGraphics, (int) mouseX, (int) mouseY);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        List<Component> components = widget.getTooltipComponents((int) mouseX, (int) mouseY);

        for (Component component : components) {
            tooltip.add(component);
        }
    }

    @NotNull
    @Override
    public ScreenPosition getPosition() {
        return position;
    }

    public Rect getRect() {
        return new Rect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }
}
