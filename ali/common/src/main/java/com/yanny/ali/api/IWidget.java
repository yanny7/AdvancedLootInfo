package com.yanny.ali.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IWidget {
    int PADDING = 2;

    RelativeRect getRect();

    WidgetDirection getDirection();

    default void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {}

    default List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return List.of();
    }
}
