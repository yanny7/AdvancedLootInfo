package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.api.IClientRegistry;
import com.yanny.advanced_loot_info.loot.LootPoolEntry;
import com.yanny.advanced_loot_info.loot.LootTableEntry;
import com.yanny.advanced_loot_info.plugin.widget.LootSlotWidget;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.WidgetUtils.*;

public class LootTableWidget extends Widget {
    private final List<Widget> widgets;
    private final Bounds bounds;

    public LootTableWidget(EmiRecipe recipe, IClientRegistry registry, LootTableEntry entry, int x, int y) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;

        widgets = new LinkedList<>();
        widgets.add(getLootTableTypeWidget(x, y));

        for (LootPoolEntry pool : entry.pools) {
            Widget widget = new LootPoolWidget(recipe, registry, pool, posX, posY, List.copyOf(entry.functions));
            Bounds bound = widget.getBounds();

            width = Math.max(width, bound.width());
            height += bound.height() + VERTICAL_OFFSET;
            posY += bound.height() + VERTICAL_OFFSET;
            widgets.add(widget);
        }

        bounds = new Bounds(x, y, width + 7, height);
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
            lastY = widget.getBounds().y();
        }

        int top = bounds.y() + 18;
        int height = lastY - bounds.y() - 9;

        guiGraphics.blitRepeating(TEXTURE_LOC, bounds.x() + 3, top, 2, height, 0, 0, 2, 18);

        for (Widget widget : widgets) {
            if (!(widget instanceof LootSlotWidget) && widget.getBounds().y() > bounds.y() + 18) {
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
    public static Bounds getBounds(IClientRegistry registry, LootTableEntry entry, int x, int y) {
        int posX = x + GROUP_WIDGET_WIDTH, posY = y;
        int width = 0, height = 0;

        for (LootPoolEntry pool : entry.pools) {
            Bounds bound = LootPoolWidget.getBounds(registry, pool, posX, posY);

            width = Math.max(width, bound.width());
            height += bound.height() + VERTICAL_OFFSET;
            posY += bound.height() + VERTICAL_OFFSET;
        }

        return new Bounds(x, y, width + 7, height);
    }
}
