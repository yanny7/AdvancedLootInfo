package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LimitCount.class)
public interface MixinLimitCount {
    @Accessor
    IntRange getLimiter();
}
