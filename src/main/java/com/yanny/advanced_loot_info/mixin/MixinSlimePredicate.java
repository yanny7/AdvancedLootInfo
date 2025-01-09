package com.yanny.advanced_loot_info.mixin;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SlimePredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SlimePredicate.class)
public interface MixinSlimePredicate {
    @Accessor
    MinMaxBounds.Ints getSize();
}
