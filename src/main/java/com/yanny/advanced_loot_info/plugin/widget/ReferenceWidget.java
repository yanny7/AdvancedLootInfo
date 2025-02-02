package com.yanny.advanced_loot_info.plugin.widget;

import com.yanny.advanced_loot_info.api.IClientRegistry;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.LootEntry;
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

public class ReferenceWidget extends Widget {
    private final Bounds bounds;
    private final Widget widget;

    public ReferenceWidget(EmiRecipe recipe, IClientRegistry registry, LootEntry entry, int x, int y, int sumWeight,
                           List<ILootFunction> functions, List<ILootCondition> conditions) {
        LootTableEntry tableEntry = ((ReferenceEntry) entry).lootTable;

        if (tableEntry != null) {
            widget = new LootTableWidget(recipe, registry, tableEntry, x, y);
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
    }

    @Override
    public Bounds getBounds() {
        return bounds;
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
    public static Bounds getBounds(IClientRegistry registry, LootEntry entry, int x, int y) {
        LootTableEntry lootTableEntry = ((ReferenceEntry) entry).lootTable;

        if (lootTableEntry != null) {
            return LootTableWidget.getBounds(registry, lootTableEntry, x, y);
        } else {
            return new Bounds(x, y, 0, 18);
        }
    }
}
