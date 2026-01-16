package com.yanny.ali.plugin.glm;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public interface ILootTableIdConditionPredicate {
    boolean isLootTableIdCondition(LootItemCondition condition);

    Identifier getTargetLootTableId(LootItemCondition condition);
}
