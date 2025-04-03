package com.yanny.ali.plugin.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.entry.ReferenceEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReferenceWidget implements IEntryWidget {
    private final Rect bounds;
    private final IWidget widget;
    private final LootPoolEntryContainer entry;

    public ReferenceWidget(IWidgetUtils utils, LootPoolEntryContainer entry, int x, int y, int sumWeight,
                           List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        LootTable tableEntry = utils.getLootTable(((LootTableReference) entry).name);

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
    public LootPoolEntryContainer getLootEntry() {
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
    public static Rect getBounds(IUtils utils, LootPoolEntryContainer entry, int x, int y) {
        LootTable lootTable = utils.getLootTable(((LootTableReference) entry).name);

        if (lootTable != null) {
            return LootTableWidget.getBounds(utils, lootTable, x, y);
        } else {
            return new Rect(x, y, 0, 18);
        }
    }
}
