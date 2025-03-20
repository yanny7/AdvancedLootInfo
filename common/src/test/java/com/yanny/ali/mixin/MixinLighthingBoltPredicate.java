package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;

public interface MixinLighthingBoltPredicate {
    MinMaxBounds.Ints getBlocksSetOnFire();
    EntityPredicate getEntityStruck();
}

