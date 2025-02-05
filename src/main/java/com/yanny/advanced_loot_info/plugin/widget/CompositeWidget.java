package com.yanny.advanced_loot_info.plugin.widget;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.*;
import com.yanny.advanced_loot_info.plugin.entry.CompositeEntry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.plugin.WidgetUtils.TEXTURE_LOC;

public class CompositeWidget extends EntryWidget {
    private final Bounds bounds;
    protected final List<Widget> widgets;
    private final ILootEntry entry;
    private final IClientUtils utils;

    public CompositeWidget(EmiRecipe recipe, IClientUtils utils, ILootEntry entry, int x, int y, int sumWeight,
                           List<ILootFunction> functions, List<ILootCondition> conditions) {
        CompositeEntry compositeEntry = (CompositeEntry) entry;
        List<ILootCondition> allConditions = new LinkedList<>(conditions);

        allConditions.addAll(compositeEntry.conditions);

        Pair<List<EntryWidget>, Bounds> pair = utils.createWidgets(recipe, utils, compositeEntry.children, x, y, List.copyOf(functions), allConditions);

        widgets = new LinkedList<>(pair.getFirst());
        bounds = pair.getSecond();
        this.entry = entry;
        this.utils = utils;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public ILootEntry getLootEntry() {
        return entry;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int lastY = 0;
        WidgetDirection lastDirection = null;

        for (Widget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY, delta);

            if (widget instanceof EntryWidget entryWidget) {
                WidgetDirection direction = utils.getWidgetDirection(entryWidget.getLootEntry());

                if (direction == WidgetDirection.VERTICAL || (lastDirection != null && direction != lastDirection)) {
                    lastY = Math.max(lastY, widget.getBounds().y());
                }

                lastDirection = direction;
            }
        }

        int top = bounds.y() + 18;
        int height = lastY - bounds.y() - 9;

        guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 3, top, 2, height, 0, 0, 2, 18);
        lastDirection = null;

        for (Widget widget : widgets) {
            if (widget instanceof EntryWidget entryWidget) {
                WidgetDirection direction = utils.getWidgetDirection(entryWidget.getLootEntry());

                if ((direction == WidgetDirection.VERTICAL || (lastDirection != null && direction != lastDirection)) && widget.getBounds().y() > bounds.y() + 18) {
                    guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 4, widget.getBounds().y() + 8, 3, 2, 2, 0, 18, 2);
                }

                lastDirection = direction;
            }
        }
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        List<ClientTooltipComponent> components = new LinkedList<>();

        for (Widget widget : widgets) {
            Bounds b = widget.getBounds();

            if (b.contains(mouseX, mouseY)) {
                components.addAll(widget.getTooltip(mouseX, mouseY));
            }
        }

        return components;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean clicked = false;

        for (Widget widget : widgets) {
            Bounds b = widget.getBounds();

            if (b.contains(mouseX, mouseY)) {
                clicked |= widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        return clicked;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean pressed = false;

        for (Widget widget : widgets) {
            pressed |= widget.keyPressed(keyCode, scanCode, modifiers);
        }

        return pressed;
    }

    @NotNull
    public static Bounds getBounds(IClientUtils utils, ILootEntry entry, int x, int y) {
        return utils.getBounds(utils, ((CompositeEntry) entry).children, x, y);
    }
}
