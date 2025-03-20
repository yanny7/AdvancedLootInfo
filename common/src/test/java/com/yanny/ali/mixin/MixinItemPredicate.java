package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface MixinItemPredicate {
    @Nullable
    TagKey<Item> getTag();

    @Nullable
    Set<Item> getItems();

    MinMaxBounds.Ints getCount();
    MinMaxBounds.Ints getDurability();
    EnchantmentPredicate[] getEnchantments();
    EnchantmentPredicate[] getStoredEnchantments();

    @Nullable
    Potion getPotion();

    NbtPredicate getNbt();
}
