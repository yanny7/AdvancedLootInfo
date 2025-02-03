package com.yanny.advanced_loot_info.plugin.widget;

import com.yanny.advanced_loot_info.api.*;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnknownWidget extends EntryWidget {
    private final Bounds bounds;
    private final LootEntry entry;

    public UnknownWidget(EmiRecipe recipe, IClientUtils utils, LootEntry entry, int x, int y, int sumWeight,
                         List<ILootFunction> functions, List<ILootCondition> conditions) {
        bounds = getBounds(utils, entry, x, y);
        this.entry = entry;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public LootEntry getLootEntry() {
        return entry;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
    }

    @NotNull
    public static Bounds getBounds(IClientUtils utils, LootEntry entry, int x, int y) {
        //FIXME make actual widget
        return new Bounds(x, y, 0, 18);
    }
}
