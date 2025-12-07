package com.yanny.ali.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IWidget {
    int PADDING = 2;

    RelativeRect getRect();

    WidgetDirection getDirection();

    default void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    default List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return List.of();
    }

    default boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }

    default void onResize(RelativeRect parent, int maxWidth) {}
}
