package com.yanny.ali.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.plugin.client.WidgetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class DynamicWidget implements IWidget {
    private final RelativeRect bounds;
    private final IWidget widget;

    public DynamicWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int ignoredMaxWidth) {
        widget = WidgetUtils.getDynamicWidget(Objects.requireNonNull(utils.lookupProvider()), rect, entry);
        bounds = widget.getRect();
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

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        widget.render(guiGraphics, mouseX, mouseY);
    }

    @NotNull
    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return widget.getTooltipComponents(mouseX, mouseY);
    }
}
