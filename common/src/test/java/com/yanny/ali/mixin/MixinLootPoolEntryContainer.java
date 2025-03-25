package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public interface MixinLootPoolEntryContainer {
    List<LootItemCondition> getConditions();
}
