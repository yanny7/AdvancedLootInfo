package com.yanny.ali.mixin;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SetStewEffectFunction.class)
public interface MixinSetStewEffectFunction {
    @Accessor
    Map<MobEffect, NumberProvider> getEffectDurationMap();
}
