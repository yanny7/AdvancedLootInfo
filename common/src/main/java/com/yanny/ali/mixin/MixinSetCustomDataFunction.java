package com.yanny.ali.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.loot.functions.SetCustomDataFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetCustomDataFunction.class)
public interface MixinSetCustomDataFunction {
    @Accessor
    CompoundTag getTag();
}
