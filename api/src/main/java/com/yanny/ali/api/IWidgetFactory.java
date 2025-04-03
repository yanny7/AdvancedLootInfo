package com.yanny.ali.api;

import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

@FunctionalInterface
public interface IWidgetFactory {
    IEntryWidget create(IWidgetUtils registry, LootPoolEntryContainer entry, int x, int y, int totalWeight,
                        List<LootItemFunction> functions, List<LootItemCondition> conditions);
}
