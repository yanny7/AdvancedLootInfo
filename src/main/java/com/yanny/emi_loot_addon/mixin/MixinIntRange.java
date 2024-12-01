package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IntRange.class)
public interface MixinIntRange {
    @Accessor
    NumberProvider getMin();

    @Accessor
    NumberProvider getMax();
}
