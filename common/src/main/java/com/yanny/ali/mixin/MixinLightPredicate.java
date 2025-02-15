package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightPredicate.class)
public interface MixinLightPredicate {
    @Accessor
    MinMaxBounds.Ints getComposite();
}
