package com.yanny.ali.plugin.mods.porting_lib.loot;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.widget.TextureWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.yanny.ali.api.ListWidget.TEXTURE_LOC;

public class GlobalLootModifierWidget implements IWidget {
    private final RelativeRect bounds;
    private final IWidget widget;

    public GlobalLootModifierWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        bounds = rect;
        widget = getGlobalLootModifierWidget(bounds, entry);
        bounds.setDimensions(widget.getRect().getWidth(), widget.getRect().getHeight());
    }

    @Override
    public RelativeRect getRect() {
        return bounds;
    }

    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.VERTICAL;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        widget.render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return widget.getTooltipComponents(mouseX, mouseY);
    }

    @NotNull
    private static IWidget getGlobalLootModifierWidget(RelativeRect rect, IDataNode node) {
        TextureWidget widget = new TextureWidget(TEXTURE_LOC, new RelativeRect(0, 0, 18, 18, rect), 84, 0);

        widget.tooltipText(node.getTooltip());
        return widget;
    }
}
