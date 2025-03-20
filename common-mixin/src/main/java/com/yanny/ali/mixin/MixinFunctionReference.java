package com.yanny.ali.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.FunctionReference;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FunctionReference.class)
public interface MixinFunctionReference {
    @Accessor
    ResourceLocation getName();
}
