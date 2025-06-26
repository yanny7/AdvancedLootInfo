package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EmptyWidget implements IEntryWidget {
    private static final ItemStack STACK = Items.BARRIER.getDefaultInstance();

    private final List<Component> components;
    private final Rect bounds;
    private final ResourceLocation id;

    public EmptyWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        bounds = getBounds(utils, entry, x, y, maxWidth);
        id = entry.getId();
        components = NodeUtils.toComponents(entry.getTooltip(), 0);
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
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return components;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.renderItem(STACK, bounds.x() + 1, bounds.y() + 1);
    }

    @NotNull
    public static Rect getBounds(IClientUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        return new Rect(x, y, 18, 18);
    }
}
