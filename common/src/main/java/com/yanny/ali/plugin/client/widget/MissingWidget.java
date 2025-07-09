package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class MissingWidget extends IWidget {
    private static final ItemStack STACK = Items.COMMAND_BLOCK.getDefaultInstance();

    private final List<Component> components;
    private final RelativeRect bounds;

    public MissingWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        this(rect);
    }

    public MissingWidget(RelativeRect rect) {
        super(MissingNode.ID);
        bounds = rect;
        bounds.setDimensions(18, 18);
        components = List.of(Component.translatable("ali.enum.group_type.missing"));
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
