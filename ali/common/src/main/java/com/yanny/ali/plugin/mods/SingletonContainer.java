package com.yanny.ali.plugin.mods;

import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public abstract class SingletonContainer extends BaseAccessor<LootPoolSingletonContainer> {
    public final int weight;
    public final int quality;
    public final List<LootItemFunction> functions;
    public final List<LootItemCondition> conditions;

    public SingletonContainer(LootPoolSingletonContainer parent) {
        super(parent);
        weight = parent.weight;
        quality = parent.quality;
        functions = parent.functions;
        conditions = parent.conditions;
    }
}
