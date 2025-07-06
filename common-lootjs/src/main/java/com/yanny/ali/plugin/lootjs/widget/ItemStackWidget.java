package com.yanny.ali.plugin.lootjs.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.lootjs.node.ItemStackNode;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class ItemStackWidget extends IWidget {
    private final Rect bounds;

    public ItemStackWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        super(entry.getId());
        ItemStackNode node = (ItemStackNode) entry;
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
