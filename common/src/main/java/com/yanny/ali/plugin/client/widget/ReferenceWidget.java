package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class ReferenceWidget implements IEntryWidget {
    private final Rect bounds;
    private final IWidget widget;
    private final LootPoolEntryContainer entry;

    public ReferenceWidget(IWidgetUtils utils, LootPoolEntryContainer entry, int x, int y, int maxWidth, int sumWeight,
                           List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        NestedLootTable reference = (NestedLootTable) entry;
        LootTable tableEntry = utils.getLootTable(reference.contents);
        List<LootItemFunction> allFunctions = new LinkedList<>(functions);
        List<LootItemCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(reference.functions);
        allConditions.addAll(reference.conditions);

        if (tableEntry != null) {
            widget = new LootTableWidget(utils, tableEntry, x, y, maxWidth, reference.quality, (float) reference.weight / sumWeight * 100, allFunctions, allConditions);
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
    public static Rect getBounds(IClientUtils utils, LootPoolEntryContainer entry, int x, int y, int maxWidth) {
        LootTable lootTable = utils.getLootTable(((NestedLootTable) entry).contents);

        if (lootTable != null) {
            return LootTableWidget.getBounds(utils, lootTable, x, y, maxWidth);
        } else {
            return new Rect(x, y, 0, 18); // FIXME
        }
    }
}
