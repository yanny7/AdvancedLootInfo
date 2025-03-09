package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.functions.CopyCustomDataFunction;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CopyCustomDataFunction.class)
public interface MixinCopyCustomDataFunction {
    @Accessor
    NbtProvider getSource();

    @Accessor
    List<CopyCustomDataFunction.CopyOperation> getOperations();
}
