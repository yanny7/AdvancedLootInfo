package com.yanny.ali.plugin.widget;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.GenericTooltipUtils;
import com.yanny.ali.plugin.TooltipUtils;
import com.yanny.ali.plugin.WidgetUtils;
import com.yanny.ali.plugin.entry.DynamicEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DynamicWidget implements IEntryWidget {
    private final List<Component> components = Lists.newArrayList();
    private final Rect bounds;
    private final IWidget widget;
    private final ILootEntry entry;

    public DynamicWidget(IWidgetUtils utils, ILootEntry entry, int x, int y, int sumWeight,
                         List<ILootFunction> functions, List<ILootCondition> conditions) {
        DynamicEntry dynamicEntry = (DynamicEntry) entry;
        List<ILootFunction> allFunctions = new LinkedList<>(functions);
        List<ILootCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(dynamicEntry.functions);
        allConditions.addAll(dynamicEntry.conditions);

        float rawChance = (float) dynamicEntry.weight / sumWeight;
        RangeValue chance = TooltipUtils.getChance(allConditions, rawChance);
        Pair<Enchantment, Map<Integer, RangeValue>> bonusChance = TooltipUtils.getBonusChance(allConditions, rawChance);

        widget = WidgetUtils.getDynamicWidget(x, y);
        bounds = widget.getRect();
        this.entry = entry;
        GenericTooltipUtils.getTooltip(utils, dynamicEntry, chance, bonusChance, new RangeValue(), null, allFunctions, allConditions).forEach(this::appendTooltip);
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        widget.render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        List<Component> components = new LinkedList<>(widget.getTooltipComponents(mouseX, mouseY));

        components.addAll(this.components);
        return components;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return widget.mouseClicked(mouseX, mouseY, button);
    }

    @NotNull
    public static Rect getBounds(IClientUtils utils, ILootEntry entry, int x, int y) {
        return new Rect(x, y, 7, 18);
    }
}
