package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.ValueCheckCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ValueCheckCondition.class)
public interface MixinValueCheckCondition {
    @Accessor
    NumberProvider getProvider();

    @Accessor
    IntRange getRange();
}
