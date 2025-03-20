package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(TimeCheck.class)
public interface MixinTimeCheck {
    @Nullable
    @Accessor
    Long getPeriod();

    @Accessor
    IntRange getValue();
}
