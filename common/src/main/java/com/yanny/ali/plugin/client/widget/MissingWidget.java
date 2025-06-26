package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MissingWidget extends IWidget {
    private static final ItemStack STACK = Items.COMMAND_BLOCK.getDefaultInstance();

    private final List<Component> components;
    private final Rect bounds;

    public MissingWidget(IWidgetUtils utils, IDataNode entry, int x, int y, int maxWidth) {
        this(x, y);
    }

    public MissingWidget(int x, int y) {
        super(MissingNode.ID);
        bounds = getBounds(x, y);
        components = List.of(Component.translatable("ali.enum.group_type.missing"));
    }

    @Override
    public Rect getRect() {
        return bounds;
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
        return getBounds(x, y);
    }

    @NotNull
    public static Rect getBounds(int x, int y) {
        return new Rect(x, y, 18, 18);
    }
}
