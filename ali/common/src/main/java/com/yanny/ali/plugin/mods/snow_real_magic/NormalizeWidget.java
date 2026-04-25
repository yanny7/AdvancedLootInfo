package com.yanny.ali.plugin.mods.snow_real_magic;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NormalizeWidget implements IWidget {
    private final RelativeRect bounds;
    private final IWidget widget;
    private final List<Component> tooltip;

    public NormalizeWidget(IWidgetUtils ignoredUtils, IDataNode entry, RelativeRect rect, int ignoredMaxWidth) {
        NormalizeNode node = (NormalizeNode) entry;

        bounds = rect;
        bounds.setDimensions(18, 18);
        widget = WidgetUtils.getUnknownWidget(rect);
        tooltip = NodeUtils.toComponents(node.getTooltip(), 0, false);
    }

    @NotNull
    @Override
    public RelativeRect getRect() {
        return bounds;
    }

    @NotNull
    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.HORIZONTAL;
    }

    @NotNull
    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return tooltip;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0, 0);
        widget.render(guiGraphics, mouseX, mouseY);
        guiGraphics.pose().popMatrix();
    }
}
