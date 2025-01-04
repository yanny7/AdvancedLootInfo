package com.yanny.emi_loot_addon.mixin;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LighthingBoltPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LighthingBoltPredicate.class)
public interface MixinLighthingBoltPredicate {
    @Accessor
    MinMaxBounds.Ints getBlocksSetOnFire();

    @Accessor
    EntityPredicate getEntityStruck();
}

