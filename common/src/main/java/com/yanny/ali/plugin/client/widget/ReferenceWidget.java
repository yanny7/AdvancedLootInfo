package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReferenceWidget implements IEntryWidget {
    private final Rect bounds;
    private final IWidget widget;
    private final ResourceLocation id;

    public ReferenceWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        if (entry instanceof ListNode listNode && !listNode.nodes().isEmpty()) {
            widget = new LootTableWidget(utils, listNode.nodes().get(0), x, y, maxWidth, entry);
        } else {
            widget = new IWidget() { //FIXME
                @Override
                public Rect getRect() {
                    return new Rect(0, 0, 0, 0);
                }

                @Override
                public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {

                }
            };
        }

        bounds = widget.getRect();
        id = entry.getId();
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
            return new Rect(x, y, 0, 18); // FIXME
        }
    }
}
