package com.yanny.ali.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public interface IItemNode {
    ItemStack getModifiedItem();

    List<LootItemCondition> getConditions();
}
