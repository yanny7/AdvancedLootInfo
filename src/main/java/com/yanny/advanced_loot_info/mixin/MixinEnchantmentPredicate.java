package com.yanny.advanced_loot_info.mixin;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(EnchantmentPredicate.class)
public interface MixinEnchantmentPredicate {
    @Nullable
    @Accessor
    Enchantment getEnchantment();

    @Accessor
    MinMaxBounds.Ints getLevel();
}
