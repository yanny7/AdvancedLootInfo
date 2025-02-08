package com.yanny.advanced_loot_info.plugin.widget;

import com.yanny.advanced_loot_info.api.*;
import com.yanny.advanced_loot_info.compatibility.emi.LootTableWidget;
import com.yanny.advanced_loot_info.loot.LootTableEntry;
import com.yanny.advanced_loot_info.plugin.entry.ReferenceEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReferenceWidget implements IEntryWidget {
    private final Rect bounds;
    private final IWidget widget;
    private final ILootEntry entry;

    public ReferenceWidget(IWidgetUtils utils, ILootEntry entry, int x, int y, int sumWeight,
                           List<ILootFunction> functions, List<ILootCondition> conditions) {
        LootTableEntry tableEntry = ((ReferenceEntry) entry).lootTable;

        if (tableEntry != null) {
            widget = new LootTableWidget(utils, tableEntry, x, y);
        } else {
            widget = new IWidget() {
                @Override
                public Rect getRect() {
                    return new Rect(0, 0, 0, 0);
                }

                @Override
                public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {

                }
            };
        }

        bounds = widget.getRect();
        this.entry = entry;
    }

    @Override
    public Rect getRect() {
        return bounds;
    }

    @Override
    public ILootEntry getLootEntry() {
        return entry;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        widget.render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return widget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return widget.getTooltipComponents(mouseX, mouseY);
    }

    @NotNull
    public static Rect getBounds(IClientUtils utils, ILootEntry entry, int x, int y) {
        LootTableEntry lootTableEntry = ((ReferenceEntry) entry).lootTable;

        if (lootTableEntry != null) {
            return LootTableWidget.getBounds(utils, lootTableEntry, x, y);
        } else {
            return new Rect(x, y, 0, 18);
        }
    }
}
