package com.yanny.advanced_loot_info.plugin.widget;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.*;
import com.yanny.advanced_loot_info.plugin.TooltipUtils;
import com.yanny.advanced_loot_info.plugin.entry.TagEntry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TagWidget extends EntryWidget {
    private final Widget widget;
    private final Bounds bounds;
    private final LootEntry entry;

    public TagWidget(EmiRecipe recipe, IClientRegistry registry, LootEntry entry, int x, int y, int sumWeight,
                     List<ILootFunction> functions, List<ILootCondition> conditions) {
        TagEntry tagEntry = (TagEntry) entry;
        List<ILootFunction> allFunctions = new LinkedList<>(functions);
        List<ILootCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(tagEntry.functions);
        allConditions.addAll(tagEntry.conditions);

        float rawChance = (float) tagEntry.weight / sumWeight;
        RangeValue chance = TooltipUtils.getChance(allConditions, rawChance);
        Pair<Enchantment, Map<Integer, RangeValue>> bonusChance = TooltipUtils.getBonusChance(allConditions, rawChance);
        RangeValue count = TooltipUtils.getCount(allFunctions);
        Pair<Enchantment, Map<Integer, RangeValue>> bonusCount = TooltipUtils.getBonusCount(allFunctions, count);

        widget = new LootSlotWidget(tagEntry, x, y, chance, bonusChance, count, bonusCount, allFunctions, allConditions).recipeContext(recipe);
        bounds = widget.getBounds();
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
        widget.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        return widget.getTooltip(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return widget.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return widget.mouseClicked(mouseX, mouseY, button);
    }

    @NotNull
    public static Bounds getBounds(IClientRegistry registry, LootEntry entry, int x, int y) {
        return new Bounds(x, y, 18, 18);
    }
}
