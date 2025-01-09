package com.yanny.advanced_loot_info.mixin;

import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DistancePredicate.class)
public interface MixinDistancePredicate {
    @Accessor
    MinMaxBounds.Doubles getX();

    @Accessor
    MinMaxBounds.Doubles getY();

    @Accessor
    MinMaxBounds.Doubles getZ();

    @Accessor
    MinMaxBounds.Doubles getHorizontal();

    @Accessor
    MinMaxBounds.Doubles getAbsolute();
}
