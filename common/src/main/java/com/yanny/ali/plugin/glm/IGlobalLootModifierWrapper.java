package com.yanny.ali.plugin.glm;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public interface IGlobalLootModifierWrapper {
    ResourceLocation getName();

    Class<?> getLootModifierClass();

    boolean isLootModifier();

    List<LootItemCondition> getConditions();

    JsonElement serialize();
}
