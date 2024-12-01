package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UniformGenerator.class)
public interface MixinUniformGenerator {
    @Accessor
    NumberProvider getMin();

    @Accessor
    NumberProvider getMax();
}
