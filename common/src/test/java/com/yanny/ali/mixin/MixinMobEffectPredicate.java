package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.world.effect.MobEffect;

import javax.annotation.Nullable;
import java.util.Map;

public interface MixinMobEffectPredicate {
    interface MobEffectInstancePredicate {
        MinMaxBounds.Ints getAmplifier();
        MinMaxBounds.Ints getDuration();

        @Nullable
        Boolean getAmbient();

        @Nullable
        Boolean getVisible();
    }

    Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> getEffects();
}
