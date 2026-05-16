package com.yanny.ali.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.client.WidgetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MissingWidget implements IWidget {
    private final List<Component> components;
    private final RelativeRect bounds;
    private final IWidget widget;

    public MissingWidget(IWidgetUtils ignoredUtils, IDataNode entry, RelativeRect rect, int ignoredMaxWidth) {
        bounds = rect;
        bounds.setDimensions(18, 18);
        components = new ArrayList<>();
        widget = WidgetUtils.getMissingWidget(rect);

        components.add(Component.translatable(Lang.Group.MISSING.singular()));
        components.addAll(CoreTooltipUtils.toComponents(entry.getTooltip(), 0, false));
    }

    public MissingWidget(RelativeRect rect) {
        bounds = rect;
        bounds.setDimensions(18, 18);
        components = List.of(Component.translatable(Lang.Group.MISSING.singular()));
        widget = WidgetUtils.getMissingWidget(rect);
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
        return components;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        widget.render(guiGraphics, mouseX, mouseY);
    }
}
