package com.yanny.ali.api;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IUtils {
    Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<LootPoolEntryContainer> entries, int x, int y,
                                                 List<LootItemFunction> functions, List<LootItemCondition> conditions);

    <T extends LootItemCondition> List<Component> getConditionTooltip(IUtils utils, int pad, T condition);
    <T extends LootItemFunction> List<Component> getFunctionTooltip(IUtils utils, int pad, T function);
    <T extends LootPoolEntryContainer> List<Item> collectItems(IUtils utils, T entry);

    Rect getBounds(IUtils registry, List<LootPoolEntryContainer> entries, int x, int y);

    @Nullable
    WidgetDirection getWidgetDirection(LootPoolEntryContainer entry);

    LootContext getLootContext();

    @Nullable
    LootTable getLootTable(ResourceLocation resourceLocation);

    RangeValue convertNumber(IUtils utils, @Nullable NumberProvider numberProvider);
}
