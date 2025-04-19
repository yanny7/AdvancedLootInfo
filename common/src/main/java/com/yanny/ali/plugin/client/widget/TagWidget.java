package com.yanny.ali.plugin.client.widget;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.TooltipUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TagWidget implements IEntryWidget {
    private final Rect bounds;
    private final LootPoolEntryContainer entry;

    public TagWidget(IWidgetUtils utils, LootPoolEntryContainer entry, int x, int y, int sumWeight,
                     List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        TagEntry tagEntry = (TagEntry) entry;
        List<LootItemFunction> allFunctions = new LinkedList<>(functions);
        List<LootItemCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(tagEntry.functions);
        allConditions.addAll(tagEntry.conditions);

        float rawChance = (float) tagEntry.weight / sumWeight;
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance = TooltipUtils.getChance(utils, allConditions, rawChance);
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> count = TooltipUtils.getCount(utils, allFunctions);

        bounds = utils.addSlotWidget(tagEntry.tag, tagEntry, x, y, chance, count, allFunctions, allConditions);
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
    public static Rect getBounds(IClientUtils utils, LootPoolEntryContainer entry, int x, int y) {
        return new Rect(x, y, 18, 18);
    }
}
