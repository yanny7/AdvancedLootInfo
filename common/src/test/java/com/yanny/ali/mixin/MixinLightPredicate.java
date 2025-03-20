package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.MinMaxBounds;

public interface MixinLightPredicate {
    MinMaxBounds.Ints getComposite();
}
