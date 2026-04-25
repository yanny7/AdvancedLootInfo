package com.yanny.ali.api;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IItemNode {
    @NotNull
    Either<ItemStack, TagKey<? extends ItemLike>> getModifiedItem();

    @NotNull
    List<LootItemCondition> getConditions();

    @NotNull
    List<LootItemFunction> getFunctions();

    @NotNull
    RangeValue getCount();

    float getChance();
}
