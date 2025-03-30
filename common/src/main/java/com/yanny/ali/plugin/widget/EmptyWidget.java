package com.yanny.ali.plugin.widget;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.GenericTooltipUtils;
import com.yanny.ali.plugin.TooltipUtils;
import com.yanny.ali.plugin.entry.EmptyEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EmptyWidget implements IEntryWidget {
    private static final ItemStack STACK = Items.BARRIER.getDefaultInstance();

    private final List<Component> components = Lists.newArrayList();
    private final Rect bounds;
    private final ILootEntry entry;

    public EmptyWidget(IWidgetUtils utils, ILootEntry entry, int x, int y, int sumWeight,
                       List<ILootFunction> functions, List<ILootCondition> conditions) {
        EmptyEntry emptyEntry = (EmptyEntry) entry;
        List<ILootFunction> allFunctions = new LinkedList<>(functions);
        List<ILootCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(emptyEntry.functions);
        allConditions.addAll(emptyEntry.conditions);

        float rawChance = (float) emptyEntry.weight / sumWeight;
        RangeValue chance = TooltipUtils.getChance(allConditions, rawChance);
        Pair<Enchantment, Map<Integer, RangeValue>> bonusChance = TooltipUtils.getBonusChance(allConditions, rawChance);

        bounds = getBounds(utils, entry, x, y);
        this.entry = entry;
        GenericTooltipUtils.getTooltip(utils, emptyEntry, chance, bonusChance, new RangeValue(), null, allFunctions, allConditions).forEach(this::appendTooltip);
    }

    public void appendTooltip(Component text) {
        this.components.add(text);
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
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return components;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.renderItem(STACK, bounds.x() + 1, bounds.y() + 1);
    }

    @NotNull
    public static Rect getBounds(IClientUtils utils, ILootEntry entry, int x, int y) {
        return new Rect(x, y, 18, 18);
    }
}
