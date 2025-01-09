package com.yanny.advanced_loot_info.mixin;

import net.minecraft.advancements.critereon.FishingHookPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingHookPredicate.class)
public interface MixinFishingHookPredicate {
    @Accessor
    boolean isInOpenWater();
}
