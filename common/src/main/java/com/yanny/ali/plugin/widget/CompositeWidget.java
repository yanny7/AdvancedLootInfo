package com.yanny.ali.plugin.widget;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.WidgetUtils.TEXTURE_LOC;

public class CompositeWidget implements IEntryWidget {
    private final Rect bounds;
    protected final List<IWidget> widgets;
    private final LootPoolEntryContainer entry;
    private final IUtils utils;

    public CompositeWidget(IWidgetUtils utils, LootPoolEntryContainer entry, int x, int y, int sumWeight,
                           List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        CompositeEntryBase compositeEntry = (CompositeEntryBase) entry;
        List<LootItemCondition> allConditions = new LinkedList<>(conditions);

        allConditions.addAll(Arrays.asList(compositeEntry.conditions));

        Pair<List<IEntryWidget>, Rect> pair = utils.createWidgets(utils, Arrays.asList(compositeEntry.children), x, y, List.copyOf(functions), allConditions);

        widgets = new LinkedList<>(pair.getFirst());
        bounds = pair.getSecond();
        this.entry = entry;
        this.utils = utils;
    }

    @Override
    public Rect getRect() {
        return bounds;
    }

    @Override
    public LootPoolEntryContainer getLootEntry() {
        return entry;
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

        WidgetUtils.blitRepeating(guiGraphics, TEXTURE_LOC, bounds.x() + 3, top, 2, height, 0, 0, 2, 18);
        lastDirection = null;

        for (IWidget widget : widgets) {
            if (widget instanceof IEntryWidget entryWidget) {
                WidgetDirection direction = utils.getWidgetDirection(entryWidget.getLootEntry());

                if ((direction == WidgetDirection.VERTICAL || (lastDirection != null && direction != lastDirection)) && widget.getRect().y() > bounds.y() + 18) {
                    WidgetUtils.blitRepeating(guiGraphics, TEXTURE_LOC, bounds.x() + 4, widget.getRect().y() + 8, 3, 2, 2, 0, 18, 2);
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
    public static Rect getBounds(IUtils utils, LootPoolEntryContainer entry, int x, int y) {
        return utils.getBounds(utils, Arrays.asList(((CompositeEntryBase) entry).children), x, y);
    }
}
