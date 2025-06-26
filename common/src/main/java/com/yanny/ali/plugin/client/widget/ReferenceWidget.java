package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.ReferenceNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReferenceWidget extends IWidget {
    private final Rect bounds;
    private final IWidget widget;

    public ReferenceWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        super(entry.getId());

        if (entry instanceof ListNode listNode && !listNode.nodes().isEmpty()) {
            widget = new LootTableWidget(utils, ((ReferenceNode) entry).nodes().get(0), x, y, maxWidth, entry);
        } else {
            widget = new MissingWidget(x, y);
        }

        bounds = widget.getRect();
    }

    @Override
    public Rect getRect() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        widget.render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return widget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return widget.getTooltipComponents(mouseX, mouseY);
    }

    @NotNull
    public static Rect getBounds(IClientUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        if (entry instanceof ListNode listNode && !listNode.nodes().isEmpty()) {
            return LootTableWidget.getBounds(utils, listNode.nodes().get(0), x, y, maxWidth);
        } else {
            return MissingWidget.getBounds(x,  y);
        }
    }
}
