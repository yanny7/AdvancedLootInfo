package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.MinMaxBounds;

public interface MixinSlimePredicate {
    MinMaxBounds.Ints getSize();
}
