package com.yanny.advanced_loot_info.compatibility;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.IClientRegistry;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.loot.LootPoolEntry;
import com.yanny.advanced_loot_info.plugin.WidgetUtils;
import com.yanny.advanced_loot_info.plugin.widget.CompositeWidget;
import com.yanny.advanced_loot_info.plugin.widget.ItemWidget;
import com.yanny.advanced_loot_info.plugin.widget.ReferenceWidget;
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

    public LootPoolWidget(EmiRecipe recipe, IClientRegistry registry, LootPoolEntry entry, int x, int y, List<ILootFunction> functions) {
        List<ILootFunction> allFunctions = new LinkedList<>(functions);

        allFunctions.addAll(entry.functions);

        Pair<List<Widget>, Bounds> info = registry.createWidgets(recipe, registry, entry.entries, x, y, allFunctions, List.copyOf(entry.conditions));

        widgets = info.getFirst();
        widgets.add(WidgetUtils.getLootPoolTypeWidget(x, y, entry.rolls, entry.bonusRolls));
        bounds = info.getSecond();
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int lastY = 0;

        for (Widget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY, delta);

            if (widget instanceof CompositeWidget || widget instanceof ReferenceWidget) {
                lastY = Math.max(lastY, widget.getBounds().y());
            }
        }

        int top = bounds.y() + 18;
        int height = lastY - bounds.y() - 9;

        guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 3, top, 2, height, 0, 0, 2, 18);

        for (Widget widget : widgets) {
            if (!(widget instanceof ItemWidget) && widget.getBounds().y() > bounds.y() + 18) {
                guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 4, widget.getBounds().y() + 8, 3, 2, 2, 0, 18, 2);
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
    public static Bounds getBounds(IClientRegistry registry, LootPoolEntry entry, int x, int y) {
        return registry.getBounds(registry, entry.entries, x, y);
    }
}
