package com.yanny.ali.plugin.mods;

import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public abstract class ConditionalFunction extends BaseAccessor<LootItemConditionalFunction> {
    protected final List<LootItemCondition> predicates;

    public ConditionalFunction(LootItemConditionalFunction parent) {
        super(parent);
        predicates = parent.predicates;
    }
}
