package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;

public interface MixinEnchantmentPredicate {
    @Nullable
    Enchantment getEnchantment();

    MinMaxBounds.Ints getLevel();
}
