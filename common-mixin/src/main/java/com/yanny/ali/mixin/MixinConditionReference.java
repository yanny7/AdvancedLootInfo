package com.yanny.ali.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.ConditionReference;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConditionReference.class)
public interface MixinConditionReference {
    @Accessor
    ResourceLocation getName();
}
