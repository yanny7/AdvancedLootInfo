package com.yanny.ali.plugin.mods.snow_real_magic;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class NormalizeWidget implements IWidget {
    private final RelativeRect bounds;
    private final IWidget widget;
    private final List<Component> tooltip;

    public NormalizeWidget(IWidgetUtils ignoredUtils, IDataNode entry, RelativeRect rect, int ignoredMaxWidth) {
        NormalizeNode node = (NormalizeNode) entry;

        bounds = rect;
        bounds.setDimensions(18, 18);
        widget = WidgetUtils.getUnknownWidget(rect, entry);
        tooltip = NodeUtils.toComponents(node.getTooltip(), 0, false);
    }

    @Override
    public RelativeRect getRect() {
        return bounds;
    }

    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.HORIZONTAL;
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return tooltip;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);
        widget.render(guiGraphics, mouseX, mouseY);
        guiGraphics.pose().popPose();
    }
}
