package com.yanny.ali.plugin.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.entry.ReferenceEntry;
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
        widget = ((ReferenceEntry) entry).lootTable.map((l) -> (IWidget) new LootTableWidget(utils, l, x, y)).orElse(new IWidget() {
            @Override
            public Rect getRect() {
                return new Rect(0, 0, 0, 0);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {}
        });

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
        return ((ReferenceEntry) entry).lootTable.map((l) -> LootTableWidget.getBounds(utils, l, x, y)).orElse(new Rect(x, y, 0, 18));
    }
}
