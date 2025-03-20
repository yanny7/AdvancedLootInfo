package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.MinMaxBounds;

public interface MixinDistancePredicate {
    MinMaxBounds.Doubles getX();
    MinMaxBounds.Doubles getY();
    MinMaxBounds.Doubles getZ();
    MinMaxBounds.Doubles getHorizontal();
    MinMaxBounds.Doubles getAbsolute();
}
