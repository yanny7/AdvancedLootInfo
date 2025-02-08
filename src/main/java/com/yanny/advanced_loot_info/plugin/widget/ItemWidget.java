package com.yanny.advanced_loot_info.plugin.widget;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.*;
import com.yanny.advanced_loot_info.plugin.TooltipUtils;
import com.yanny.advanced_loot_info.plugin.entry.ItemEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ItemWidget implements IEntryWidget {
    private final Rect bounds;
    private final ILootEntry entry;

    public ItemWidget(IWidgetUtils utils, ILootEntry entry, int x, int y, int sumWeight,
                      List<ILootFunction> functions, List<ILootCondition> conditions) {
        ItemEntry itemEntry = (ItemEntry) entry;
        List<ILootFunction> allFunctions = new LinkedList<>(functions);
        List<ILootCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(itemEntry.functions);
        allConditions.addAll(itemEntry.conditions);

        float rawChance = (float) itemEntry.weight / sumWeight;
        RangeValue chance = TooltipUtils.getChance(allConditions, rawChance);
        Pair<Enchantment, Map<Integer, RangeValue>> bonusChance = TooltipUtils.getBonusChance(allConditions, rawChance);
        RangeValue count = TooltipUtils.getCount(allFunctions);
        Pair<Enchantment, Map<Integer, RangeValue>> bonusCount = TooltipUtils.getBonusCount(allFunctions, count);

        bounds = utils.addSlotWidget(itemEntry.item, itemEntry, x, y, chance, bonusChance, count, bonusCount, allFunctions, allConditions);
        this.entry = entry;
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
    }

    @NotNull
    public static Rect getBounds(IClientUtils utils, ILootEntry entry, int x, int y) {
        return new Rect(x, y, 18, 18);
    }
}
