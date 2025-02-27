package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SetStewEffectFunction.class)
public interface MixinSetStewEffectFunction {
    @Accessor
    List<SetStewEffectFunction.EffectEntry> getEffects();
}
