package com.yanny.ali.plugin.widget;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.GenericTooltipUtils;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EmptyWidget implements IEntryWidget {
    private static final ItemStack STACK = Items.BARRIER.getDefaultInstance();

    private final List<Component> components = Lists.newArrayList();
    private final Rect bounds;
    private final LootPoolEntryContainer entry;

    public EmptyWidget(IWidgetUtils utils, LootPoolEntryContainer entry, int x, int y, int sumWeight,
                       List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        EmptyLootItem emptyEntry = (EmptyLootItem) entry;
        List<LootItemFunction> allFunctions = new LinkedList<>(functions);
        List<LootItemCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(Arrays.asList(emptyEntry.functions));
        allConditions.addAll(Arrays.asList(emptyEntry.conditions));

        float rawChance = (float) emptyEntry.weight / sumWeight;
        RangeValue chance = TooltipUtils.getChance(allConditions, rawChance);
        Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance = TooltipUtils.getBonusChance(allConditions, rawChance);

        bounds = getBounds(utils, entry, x, y);
        this.entry = entry;
        GenericTooltipUtils.getTooltip(utils, emptyEntry, chance, bonusChance, new RangeValue(), Optional.empty(), allFunctions, allConditions).forEach(this::appendTooltip);
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
    public List<Component> getTooltipComponents(int mouseX, int mouseY) {
        return components;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.renderItem(STACK, bounds.x() + 1, bounds.y() + 1);
    }

    @NotNull
    public static Rect getBounds(IUtils utils, LootPoolEntryContainer entry, int x, int y) {
        return new Rect(x, y, 18, 18);
    }
}
