package com.yanny.ali.plugin.widget;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.GenericTooltipUtils;
import com.yanny.ali.plugin.TooltipUtils;
import com.yanny.ali.plugin.WidgetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DynamicWidget implements IEntryWidget {
    private final List<Component> components = Lists.newArrayList();
    private final Rect bounds;
    private final IWidget widget;
    private final LootPoolEntryContainer entry;

    public DynamicWidget(IWidgetUtils utils, LootPoolEntryContainer entry, int x, int y, int sumWeight,
                         List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        DynamicLoot dynamicEntry = (DynamicLoot) entry;
        List<LootItemFunction> allFunctions = new LinkedList<>(functions);
        List<LootItemCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(Arrays.asList(dynamicEntry.functions));
        allConditions.addAll(Arrays.asList(dynamicEntry.conditions));

        float rawChance = (float) dynamicEntry.weight / sumWeight;
        RangeValue chance = TooltipUtils.getChance(allConditions, rawChance);
        Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance = TooltipUtils.getBonusChance(allConditions, rawChance);

        widget = WidgetUtils.getDynamicWidget(x, y);
        bounds = widget.getRect();
        this.entry = entry;
        GenericTooltipUtils.getTooltip(utils, dynamicEntry, chance, bonusChance, new RangeValue(), Optional.empty(), allFunctions, allConditions).forEach(this::appendTooltip);
    }

    public void appendTooltip(Component text) {
        this.components.add(text);
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
    public static Rect getBounds(IUtils utils, LootPoolEntryContainer entry, int x, int y) {
        return new Rect(x, y, 7, 18);
    }
}
