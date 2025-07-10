package com.yanny.ali.api;

import com.mojang.datafixers.util.Either;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public interface IItemNode {
    Either<ItemStack, TagKey<Item>> getModifiedItem();

    List<LootItemCondition> getConditions();

    RangeValue getCount();
}
