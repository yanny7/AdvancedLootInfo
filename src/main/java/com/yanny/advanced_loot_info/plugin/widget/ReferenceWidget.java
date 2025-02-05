package com.yanny.advanced_loot_info.plugin.widget;

import com.yanny.advanced_loot_info.api.*;
import com.yanny.advanced_loot_info.compatibility.LootTableWidget;
import com.yanny.advanced_loot_info.loot.LootTableEntry;
import com.yanny.advanced_loot_info.plugin.entry.ReferenceEntry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReferenceWidget extends EntryWidget {
    private final Bounds bounds;
    private final Widget widget;
    private final ILootEntry entry;

    public ReferenceWidget(EmiRecipe recipe, IClientUtils utils, ILootEntry entry, int x, int y, int sumWeight,
                           List<ILootFunction> functions, List<ILootCondition> conditions) {
        LootTableEntry tableEntry = ((ReferenceEntry) entry).lootTable;

        if (tableEntry != null) {
            widget = new LootTableWidget(recipe, utils, tableEntry, x, y);
        } else {
            widget = new Widget() {
                @Override
                public Bounds getBounds() {
                    return new Bounds(0, 0, 0, 0);
                }

                @Override
                public void render(GuiGraphics guiGraphics, int i, int i1, float v) {

                }
            };
        }

        bounds = widget.getBounds();
        this.entry = entry;
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
        widget.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return widget.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return widget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        return widget.getTooltip(mouseX, mouseY);
    }

    @NotNull
    public static Bounds getBounds(IClientUtils utils, ILootEntry entry, int x, int y) {
        LootTableEntry lootTableEntry = ((ReferenceEntry) entry).lootTable;

        if (lootTableEntry != null) {
            return LootTableWidget.getBounds(utils, lootTableEntry, x, y);
        } else {
            return new Bounds(x, y, 0, 18);
        }
    }
}
