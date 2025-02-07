package com.yanny.advanced_loot_info.compatibility.emi;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.EntryWidget;
import com.yanny.advanced_loot_info.api.IClientUtils;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.WidgetDirection;
import com.yanny.advanced_loot_info.loot.LootPoolEntry;
import com.yanny.advanced_loot_info.plugin.WidgetUtils;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.plugin.WidgetUtils.TEXTURE_LOC;

public class LootPoolWidget extends Widget {
    private final List<Widget> widgets;
    private final Bounds bounds;
    private final IClientUtils utils;

    public LootPoolWidget(EmiRecipe recipe, IClientUtils utils, LootPoolEntry entry, int x, int y, List<ILootFunction> functions) {
        List<ILootFunction> allFunctions = new LinkedList<>(functions);

        allFunctions.addAll(entry.functions);

        Pair<List<EntryWidget>, Bounds> info = utils.createWidgets(recipe, utils, entry.entries, x, y, allFunctions, List.copyOf(entry.conditions));

        widgets = new LinkedList<>(info.getFirst());
        widgets.add(WidgetUtils.getLootPoolTypeWidget(x, y, entry.rolls, entry.bonusRolls));
        bounds = info.getSecond();
        this.utils = utils;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
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
    public static Bounds getBounds(IClientUtils registry, LootPoolEntry entry, int x, int y) {
        return registry.getBounds(registry, entry.entries, x, y);
    }
}
