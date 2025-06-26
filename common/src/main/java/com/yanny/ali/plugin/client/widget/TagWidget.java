package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.TagNode;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class TagWidget extends IWidget {
    private final Rect bounds;

    public TagWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        super(entry.getId());
        TagNode tagEntry = (TagNode) entry;

        bounds = utils.addSlotWidget(tagEntry.getTag(), tagEntry, x, y);
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
