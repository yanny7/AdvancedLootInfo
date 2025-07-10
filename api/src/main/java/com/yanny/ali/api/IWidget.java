package com.yanny.ali.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public abstract class IWidget {
    private final ResourceLocation id;

    public IWidget(ResourceLocation id) {
        this.id = id;
    }

    public abstract RelativeRect getRect();

    public abstract WidgetDirection getDirection();

    public ResourceLocation getNodeId() {
        return id;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return List.of();
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }
}
