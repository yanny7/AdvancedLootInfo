package com.yanny.ali.plugin.widget;

import com.google.common.collect.Lists;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.WidgetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class DynamicWidget implements IEntryWidget {
    private final List<Component> components = Lists.newArrayList();
    private final Rect bounds;
    private final IWidget widget;
    private final LootPoolEntryContainer entry;

    public DynamicWidget(IWidgetUtils utils, LootPoolEntryContainer entry, int x, int y, int sumWeight,
                         List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        widget = WidgetUtils.getDynamicWidget(x, y, (DynamicLoot) entry, sumWeight);
        bounds = widget.getRect();
        this.entry = entry;
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
