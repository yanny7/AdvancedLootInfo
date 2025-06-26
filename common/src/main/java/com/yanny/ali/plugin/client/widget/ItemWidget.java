package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class ItemWidget extends IWidget {
    private final Rect bounds;

    public ItemWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        super(entry.getId());
        ItemNode node = (ItemNode) entry;
        bounds = utils.addSlotWidget(node.getModifiedItem(), node, x, y);
    }

    @Override
    public Rect getRect() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @NotNull
    public static Rect getBounds(IClientUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        return new Rect(x, y, 18, 18);
    }
}
