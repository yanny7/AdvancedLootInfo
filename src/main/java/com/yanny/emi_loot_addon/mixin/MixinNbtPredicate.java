package com.yanny.emi_loot_addon.mixin;

import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NbtPredicate.class)
public interface MixinNbtPredicate {
    @Accessor
    CompoundTag getTag();
}
