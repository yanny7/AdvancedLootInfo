package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class DynamicWidget implements IEntryWidget {
    private final List<Component> components;
    private final Rect bounds;
    private final IWidget widget;
    private final ResourceLocation id;

    public DynamicWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        widget = WidgetUtils.getDynamicWidget(x, y, entry);
        bounds = widget.getRect();
        id = entry.getId();
        components = NodeUtils.toComponents(entry.getTooltip(), 0);
    }

    @Override
    public Rect getRect() {
        return bounds;
    }

    @Override
    public ResourceLocation getNodeId() {
        return id;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        widget.render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        List<Component> components = new LinkedList<>(widget.getTooltipComponents(mouseX, mouseY));

        components.addAll(this.components);
        return components;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return widget.mouseClicked(mouseX, mouseY, button);
    }

    @NotNull
    public static Rect getBounds(IClientUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        return new Rect(x, y, 7, 18);
    }
}
