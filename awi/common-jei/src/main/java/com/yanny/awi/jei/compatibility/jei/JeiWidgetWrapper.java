package com.yanny.awi.jei.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.compatibility.GuiGraphics;
import com.yanny.aci.compatibility.ScrollableTooltip;
import com.yanny.aci.tooltip.TooltipLine;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JeiWidgetWrapper implements IRecipeWidget {
    private final IWidget widget;
    private final Rect2i position;
    private final RelativeRect rect;

    public JeiWidgetWrapper(IWidget widget) {
        this.rect = widget.getRect();
        this.widget = widget;
        position = new Rect2i(0, 0, 0, 0);
    }

    @Override
    public void drawWidget(PoseStack pose, double mouseX, double mouseY) {
        GuiGraphics guiGraphics = new GuiGraphics(pose);
        widget.render(guiGraphics, (int) mouseX, (int) mouseY);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        List<TooltipLine> components = widget.getTooltipLines((int) mouseX, (int) mouseY);

        if (!components.isEmpty()) {
            tooltip.add(new ScrollableTooltip(components));
        }
    }

    @NotNull
    @Override
    public Rect2i getArea() {
        return position;
    }

    public Rect getRect() {
        return new Rect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }
}
