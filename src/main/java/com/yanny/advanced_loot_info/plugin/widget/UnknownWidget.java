package com.yanny.advanced_loot_info.plugin.widget;

import com.yanny.advanced_loot_info.api.IClientRegistry;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.LootEntry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnknownWidget extends Widget {
    private final Bounds bounds;

    public UnknownWidget(EmiRecipe recipe, IClientRegistry registry, LootEntry entry, int x, int y, int sumWeight,
                         List<ILootFunction> functions, List<ILootCondition> conditions) {
        bounds = getBounds(registry, entry, x, y);
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
    }

    @NotNull
    public static Bounds getBounds(IClientRegistry registry, LootEntry entry, int x, int y) {
        return new Bounds(x, y, 0, 18);
    }
}
