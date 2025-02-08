package com.yanny.advanced_loot_info.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IWidget {
    Rect getRect();

    void render(GuiGraphics guiGraphics, int mouseX, int mouseY);

    default List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return List.of();
    }

    default boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }
}
