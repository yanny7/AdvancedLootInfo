package com.yanny.advanced_loot_info.mixin;

import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IntRange.class)
public interface MixinIntRange {
    @Nullable
    @Accessor
    NumberProvider getMin();

    @Nullable
    @Accessor
    NumberProvider getMax();
}
