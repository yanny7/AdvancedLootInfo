package com.yanny.ali.plugin.glm;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public interface IGlobalLootModifierWrapper {
    Identifier getName();

    Class<?> getLootModifierClass();

    boolean isLootModifier();

    List<LootItemCondition> getConditions();
}
