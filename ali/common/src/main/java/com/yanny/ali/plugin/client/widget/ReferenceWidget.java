package com.yanny.ali.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.plugin.common.nodes.ReferenceNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ReferenceWidget implements IWidget {
    private final RelativeRect bounds;
    private final IWidget widget;

    public ReferenceWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        if (entry instanceof ListNode listNode && !listNode.nodes().isEmpty()) {
            widget = new LootTableWidget(utils, ((ReferenceNode) entry).nodes().getFirst(), rect, maxWidth, entry);
        } else {
            widget = new MissingWidget(Objects.requireNonNull(utils.lookupProvider()), rect);
        }

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
        return WidgetDirection.VERTICAL;
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
