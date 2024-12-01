package com.yanny.emi_loot_addon.mixin;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ItemPredicate.class)
public interface MixinItemPredicate {
    @Nullable
    @Accessor
    TagKey<Item> getTag();

    @Nullable
    @Accessor
    Set<Item> getItems();

    @Accessor
    MinMaxBounds.Ints getCount();

    @Accessor
    MinMaxBounds.Ints getDurability();

    @Accessor
    EnchantmentPredicate[] getEnchantments();

    @Accessor
    EnchantmentPredicate[] getStoredEnchantments();

    @Nullable
    @Accessor
    Potion getPotion();

    @Accessor
    NbtPredicate getNbt();
}
