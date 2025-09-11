package com.yanny.ali.plugin.mods;

import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class ConditionalFunction extends BaseAccessor<LootItemConditionalFunction> {
    protected final LootItemCondition[] predicates;

    public ConditionalFunction(LootItemConditionalFunction parent) {
        super(parent);
        predicates = parent.predicates;
    }
}
