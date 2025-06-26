package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ItemWidget implements IEntryWidget {
    private final Rect bounds;
    private final ResourceLocation id;

    public ItemWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        ItemNode node = (ItemNode) entry;
        bounds = utils.addSlotWidget(node.getModifiedItem(), entry, x, y);
        this.id = entry.getId();
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
    }

    @NotNull
    public static Rect getBounds(IClientUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        return new Rect(x, y, 18, 18);
    }
}
