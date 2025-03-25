package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.functions.SetCustomModelDataFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetCustomModelDataFunction.class)
public interface MixinSetCustomModelDataFunction {
    @Accessor
    NumberProvider getValueProvider();
}
