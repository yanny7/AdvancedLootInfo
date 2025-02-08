package com.yanny.advanced_loot_info.plugin.widget;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.*;
import com.yanny.advanced_loot_info.plugin.TooltipUtils;
import com.yanny.advanced_loot_info.plugin.entry.EmptyEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EmptyWidget implements IEntryWidget {
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
        utils.addSlotWidget(emptyEntry, x, y, chance, bonusChance, new RangeValue(), null, allFunctions, allConditions);
        this.entry = entry;
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
    }

    @NotNull
    public static Rect getBounds(IClientUtils utils, ILootEntry entry, int x, int y) {
        return new Rect(x, y, 18, 18);
    }
}
