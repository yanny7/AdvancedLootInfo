package com.yanny.emi_loot_addon.mixin;

import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CopyNameFunction.class)
public interface MixinCopyNameFunction {
    @Accessor
    CopyNameFunction.NameSource getSource();
}
