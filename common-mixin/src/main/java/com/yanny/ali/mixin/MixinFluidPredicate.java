package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FluidPredicate.class)
public interface MixinFluidPredicate {
    @Nullable
    @Accessor
    TagKey<Fluid> getTag();

    @Nullable
    @Accessor
    Fluid getFluid();

    @Accessor
    StatePropertiesPredicate getProperties();
}
