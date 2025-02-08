package com.yanny.advanced_loot_info.compatibility.emi;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.*;
import com.yanny.advanced_loot_info.loot.LootPoolEntry;
import com.yanny.advanced_loot_info.plugin.WidgetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.plugin.WidgetUtils.TEXTURE_LOC;

public class LootPoolWidget implements IWidget {
    private final List<IWidget> widgets;
    private final Rect bounds;
    private final IClientUtils utils;

    public LootPoolWidget(IWidgetUtils utils, LootPoolEntry entry, int x, int y, List<ILootFunction> functions) {
        List<ILootFunction> allFunctions = new LinkedList<>(functions);

        allFunctions.addAll(entry.functions);

        Pair<List<IEntryWidget>, Rect> info = utils.createWidgets(utils, entry.entries, x, y, allFunctions, List.copyOf(entry.conditions));

        widgets = new LinkedList<>(info.getFirst());
        widgets.add(WidgetUtils.getLootPoolTypeWidget(x, y, entry.rolls, entry.bonusRolls));
        bounds = info.getSecond();
        this.utils = utils;
    }

    @Override
    public Rect getRect() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int lastY = 0;
        WidgetDirection lastDirection = null;

        for (IWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY);

            if (widget instanceof IEntryWidget entryWidget) {
                WidgetDirection direction = utils.getWidgetDirection(entryWidget.getLootEntry());

                if (direction == WidgetDirection.VERTICAL || (lastDirection != null && direction != lastDirection)) {
                    lastY = Math.max(lastY, widget.getRect().y());
                }

                lastDirection = direction;
            }
        }

        int top = bounds.y() + 18;
        int height = lastY - bounds.y() - 9;

        guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 3, top, 2, height, 0, 0, 2, 18);
        lastDirection = null;

        for (IWidget widget : widgets) {
            if (widget instanceof IEntryWidget entryWidget) {
                WidgetDirection direction = utils.getWidgetDirection(entryWidget.getLootEntry());

                if ((direction == WidgetDirection.VERTICAL || (lastDirection != null && direction != lastDirection)) && widget.getRect().y() > bounds.y() + 18) {
                    guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 4, widget.getRect().y() + 8, 3, 2, 2, 0, 18, 2);
                }

                lastDirection = direction;
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
    public static Rect getBounds(IClientUtils registry, LootPoolEntry entry, int x, int y) {
        return registry.getBounds(registry, entry.entries, x, y);
    }
}
