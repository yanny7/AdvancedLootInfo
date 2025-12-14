package com.yanny.ali.plugin.glm;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public interface ILootTableIdConditionPredicate {
    boolean isLootTableIdCondition(LootItemCondition condition);

    ResourceLocation getTargetLootTableId(LootItemCondition condition);
}
