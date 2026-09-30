package com.yanny.aci.api;

import com.yanny.aci.tooltip.TooltipLine;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IWidget {
    int PADDING = 2;

    @NotNull
    RelativeRect getRect();

    @NotNull
    WidgetDirection getDirection();

    default void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    @NotNull
    default List<TooltipLine> getTooltipLines(int mouseX, int mouseY) {
        return List.of();
    }
}
