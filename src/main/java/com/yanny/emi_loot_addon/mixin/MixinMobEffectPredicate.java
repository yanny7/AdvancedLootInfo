package com.yanny.emi_loot_addon.mixin;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;
import java.util.Map;

@Mixin(MobEffectsPredicate.class)
public interface MixinMobEffectPredicate {
    @Mixin(MobEffectsPredicate.MobEffectInstancePredicate.class)
    interface MobEffectInstancePredicate {
        @Accessor
        MinMaxBounds.Ints getAmplifier();

        @Accessor
        MinMaxBounds.Ints getDuration();

        @Nullable
        @Accessor
        Boolean getAmbient();

        @Nullable
        @Accessor
        Boolean getVisible();
    }

    @Accessor
    Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> getEffects();
}
