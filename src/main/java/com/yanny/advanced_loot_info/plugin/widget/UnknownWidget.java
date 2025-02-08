package com.yanny.advanced_loot_info.plugin.widget;

import com.yanny.advanced_loot_info.api.*;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnknownWidget implements IEntryWidget {
    private final Rect bounds;
    private final ILootEntry entry;

    public UnknownWidget(IWidgetUtils utils, ILootEntry entry, int x, int y, int sumWeight,
                         List<ILootFunction> functions, List<ILootCondition> conditions) {
        bounds = getBounds(utils, entry, x, y);
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
    }

    @NotNull
    public static Rect getBounds(IClientUtils utils, ILootEntry entry, int x, int y) {
        //FIXME make actual widget
        return new Rect(x, y, 0, 18);
    }
}
