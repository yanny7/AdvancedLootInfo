package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public interface MixinFluidPredicate {
    @Nullable
    TagKey<Fluid> getTag();
    @Nullable
    Fluid getFluid();
    StatePropertiesPredicate getProperties();
}
