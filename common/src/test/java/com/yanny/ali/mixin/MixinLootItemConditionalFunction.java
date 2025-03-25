package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public interface MixinLootItemConditionalFunction {
    LootItemCondition[] getPredicates();
}
