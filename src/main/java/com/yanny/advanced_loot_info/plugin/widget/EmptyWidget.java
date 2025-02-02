package com.yanny.advanced_loot_info.plugin.widget;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.*;
import com.yanny.advanced_loot_info.plugin.TooltipUtils;
import com.yanny.advanced_loot_info.plugin.entry.EmptyEntry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EmptyWidget extends Widget {
    private final List<Supplier<ClientTooltipComponent>> tooltipSuppliers = Lists.newArrayList();
    private final Bounds bounds;
    private final Widget widget;

    public EmptyWidget(EmiRecipe recipe, IClientRegistry registry, LootEntry entry, int x, int y, int sumWeight,
                       List<ILootFunction> functions, List<ILootCondition> conditions) {
        EmptyEntry emptyEntry = (EmptyEntry) entry;
        List<ILootFunction> allFunctions = new LinkedList<>(functions);
        List<ILootCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(emptyEntry.functions);
        allConditions.addAll(emptyEntry.conditions);

        float rawChance = (float) emptyEntry.weight / sumWeight;
        RangeValue chance = TooltipUtils.getChance(allConditions, rawChance);
        Pair<Enchantment, Map<Integer, RangeValue>> bonusChance = TooltipUtils.getBonusChance(allConditions, rawChance);

        bounds = getBounds(registry, entry, x, y);
        widget = new SlotWidget(EmiStack.of(Items.BARRIER), x, y).drawBack(false);
        setupTooltip(emptyEntry, chance, bonusChance, allFunctions, allConditions);
    }

    private void setupTooltip(EmptyEntry entry, RangeValue chance, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance,
                              List<ILootFunction> functions, List<ILootCondition> conditions) {
        TooltipUtils.getQuality(entry).forEach(this::appendTooltip);

        appendTooltip(TooltipUtils.getChance(chance));
        TooltipUtils.getBonusChance(bonusChance).forEach(this::appendTooltip);

        TooltipUtils.getConditions(conditions, 0).forEach(this::appendTooltip);
        TooltipUtils.getFunctions(functions, 0).forEach(this::appendTooltip);
    }

    public void appendTooltip(Component text) {
        this.tooltipSuppliers.add(() -> ClientTooltipComponent.create(text.getVisualOrderText()));
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        List<ClientTooltipComponent> components = new LinkedList<>();

        for(Supplier<ClientTooltipComponent> supplier : this.tooltipSuppliers) {
            components.add(supplier.get());
        }

        return components;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        widget.render(guiGraphics, mouseX, mouseY, delta);
    }

    @NotNull
    public static Bounds getBounds(IClientRegistry registry, LootEntry entry, int x, int y) {
        return new Bounds(x, y, 18, 18);
    }
}
