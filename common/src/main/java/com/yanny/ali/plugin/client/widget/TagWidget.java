package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.TagNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TagWidget implements IEntryWidget {
    private final Rect bounds;
    private final ResourceLocation id;

    public TagWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        TagNode tagEntry = (TagNode) entry;

        bounds = utils.addSlotWidget(tagEntry.getTag(), tagEntry, x, y);
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
