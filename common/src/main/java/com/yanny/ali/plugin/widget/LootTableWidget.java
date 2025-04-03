package com.yanny.ali.plugin.widget;

import com.yanny.ali.api.IUtils;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.Rect;
import com.yanny.ali.plugin.WidgetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.WidgetUtils.*;

public class LootTableWidget implements IWidget {
    private final List<IWidget> widgets;
    private final Rect bounds;

    public LootTableWidget(IWidgetUtils utils, LootTable lootTable, int x, int y) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;

        widgets = new LinkedList<>();
        widgets.add(getLootTableTypeWidget(x, y));

        for (LootPool pool : lootTable.pools) {
            IWidget widget = new LootPoolWidget(utils, pool, posX, posY, List.copyOf(lootTable.functions));
            Rect bound = widget.getRect();

            width = Math.max(width, bound.width());
            height += bound.height() + VERTICAL_OFFSET;
            posY += bound.height() + VERTICAL_OFFSET;
            widgets.add(widget);
        }

        bounds = new Rect(x, y, width + 7, height);
    }

    @Override
    public Rect getRect() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int lastY = 0;

        for (IWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY);
            lastY = widget.getRect().y();
        }

        int top = bounds.y() + 18;
        int height = lastY - bounds.y() - 9;

        WidgetUtils.blitRepeating(guiGraphics, TEXTURE_LOC, bounds.x() + 3, top, 2, height, 0, 0, 2, 18);

        for (IWidget widget : widgets) {
            if (widget.getRect().y() > bounds.y() + 18) {
                WidgetUtils.blitRepeating(guiGraphics, TEXTURE_LOC, bounds.x() + 4, widget.getRect().y() + 8, 3, 2, 2, 0, 18, 2);
            }
        }
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        List<Component> components = new LinkedList<>();

        for (IWidget widget : widgets) {
            Rect b = widget.getRect();

            if (b.contains(mouseX, mouseY)) {
                components.addAll(widget.getTooltipComponents(mouseX, mouseY));
            }
        }

        return components;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean clicked = false;

        for (IWidget widget : widgets) {
            Rect b = widget.getRect();

            if (b.contains(mouseX, mouseY)) {
                clicked |= widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        return clicked;
    }

    @NotNull
    public static Rect getBounds(IUtils utils, LootTable lootTable, int x, int y) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;

        for (LootPool pool : lootTable.pools) {
            Rect bound = LootPoolWidget.getBounds(utils, pool, posX, posY);

            width = Math.max(width, bound.width());
            height += bound.height() + VERTICAL_OFFSET;
            posY += bound.height() + VERTICAL_OFFSET;
        }

        if (height == 0) {
            height = 18;
        }

        return new Rect(x, y, width + 7, height);
    }
}
