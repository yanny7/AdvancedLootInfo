package com.yanny.ali.plugin.mods;

import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class SingletonContainer {
    public final int weight;
    public final int quality;
    public final LootItemFunction[] functions;
    public final LootItemCondition[] conditions;

    public SingletonContainer(LootPoolSingletonContainer entry) {
        weight = entry.weight;
        quality = entry.quality;
        functions = entry.functions;
        conditions = entry.conditions;
    }
}
