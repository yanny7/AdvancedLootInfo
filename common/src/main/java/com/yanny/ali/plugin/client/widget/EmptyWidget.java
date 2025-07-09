package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class EmptyWidget extends IWidget {
    private static final ItemStack STACK = Items.BARRIER.getDefaultInstance();

    private final List<Component> components;
    private final RelativeRect bounds;

    public EmptyWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(entry.getId());
        bounds = rect;
        bounds.setDimensions(18, 18);
        components = NodeUtils.toComponents(entry.getTooltip(), 0);
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
        return components;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.renderItem(STACK, bounds.getX() + 1, bounds.getY() + 1);
    }
}
