package com.yanny.advanced_loot_info.mixin;

import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FluidPredicate.class)
public interface MixinFluidPredicate {
    @Accessor
    TagKey<Block> getTag();

    @Accessor
    Fluid getFluid();

    @Accessor
    StatePropertiesPredicate getProperties();
}
