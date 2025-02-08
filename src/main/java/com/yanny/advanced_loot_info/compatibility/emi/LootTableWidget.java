package com.yanny.advanced_loot_info.compatibility.emi;

import com.yanny.advanced_loot_info.api.IClientUtils;
import com.yanny.advanced_loot_info.api.IWidget;
import com.yanny.advanced_loot_info.api.IWidgetUtils;
import com.yanny.advanced_loot_info.api.Rect;
import com.yanny.advanced_loot_info.loot.LootPoolEntry;
import com.yanny.advanced_loot_info.loot.LootTableEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.plugin.WidgetUtils.*;

public class LootTableWidget implements IWidget {
    private final List<IWidget> widgets;
    private final Rect bounds;

    public LootTableWidget(IWidgetUtils utils, LootTableEntry entry, int x, int y) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;

        widgets = new LinkedList<>();
        widgets.add(getLootTableTypeWidget(x, y));

        for (LootPoolEntry pool : entry.pools) {
            IWidget widget = new LootPoolWidget(utils, pool, posX, posY, List.copyOf(entry.functions));
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

        guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 3, top, 2, height, 0, 0, 2, 18);

        for (IWidget widget : widgets) {
            if (widget.getRect().y() > bounds.y() + 18) {
                guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 4, widget.getRect().y() + 8, 3, 2, 2, 0, 18, 2);
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
    public static Rect getBounds(IClientUtils utils, LootTableEntry entry, int x, int y) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;

        for (LootPoolEntry pool : entry.pools) {
            Rect bound = LootPoolWidget.getBounds(utils, pool, posX, posY);

            width = Math.max(width, bound.width());
            height += bound.height() + VERTICAL_OFFSET;
            posY += bound.height() + VERTICAL_OFFSET;
        }

        return new Rect(x, y, width + 7, height);
    }
}
