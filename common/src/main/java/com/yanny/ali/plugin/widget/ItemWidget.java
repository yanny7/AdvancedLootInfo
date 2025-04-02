package com.yanny.ali.plugin.widget;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ItemWidget implements IEntryWidget {
    private final Rect bounds;
    private final LootPoolEntryContainer entry;

    public ItemWidget(IWidgetUtils utils, LootPoolEntryContainer entry, int x, int y, int sumWeight,
                      List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        LootItem itemEntry = (LootItem) entry;
        List<LootItemFunction> allFunctions = new LinkedList<>(functions);
        List<LootItemCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(Arrays.asList(itemEntry.functions));
        allConditions.addAll(Arrays.asList(itemEntry.conditions));

        float rawChance = (float) itemEntry.weight / sumWeight;
        RangeValue chance = TooltipUtils.getChance(allConditions, rawChance);
        Pair<Enchantment, Map<Integer, RangeValue>> bonusChance = TooltipUtils.getBonusChance(allConditions, rawChance);
        RangeValue count = TooltipUtils.getCount(utils, allFunctions);
        Pair<Enchantment, Map<Integer, RangeValue>> bonusCount = TooltipUtils.getBonusCount(utils, allFunctions, count);

        bounds = utils.addSlotWidget(itemEntry.item, itemEntry, x, y, chance, bonusChance, count, bonusCount, allFunctions, allConditions);
        this.entry = entry;
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
    }

    @NotNull
    public static Rect getBounds(IUtils utils, LootPoolEntryContainer entry, int x, int y) {
        return new Rect(x, y, 18, 18);
    }
}
