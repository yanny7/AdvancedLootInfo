package com.yanny.ali.plugin.client.widget;

import com.google.common.collect.Lists;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IEntryWidget;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.Rect;
import com.yanny.ali.plugin.client.EntryTooltipUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EmptyWidget implements IEntryWidget {
    private static final ItemStack STACK = Items.BARRIER.getDefaultInstance();

    private final List<Component> components = Lists.newArrayList();
    private final Rect bounds;
    private final LootPoolEntryContainer entry;

    public EmptyWidget(IWidgetUtils utils, LootPoolEntryContainer entry, int x, int y, int sumWeight,
                       List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        bounds = getBounds(utils, entry, x, y);
        this.entry = entry;
        EntryTooltipUtils.getEmptyTooltip(utils, (EmptyLootItem) entry, sumWeight, functions, conditions).forEach(this::appendTooltip);
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
    public static Rect getBounds(IClientUtils utils, LootPoolEntryContainer entry, int x, int y) {
        return new Rect(x, y, 18, 18);
    }
}
