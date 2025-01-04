package com.yanny.emi_loot_addon.mixin;

import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(LocationPredicate.class)
public interface MixinLocationPredicate {
    @Accessor
    MinMaxBounds.Doubles getX();

    @Accessor
    MinMaxBounds.Doubles getY();

    @Accessor
    MinMaxBounds.Doubles getZ();

    @Nullable
    @Accessor
    ResourceKey<Biome> getBiome();

    @Nullable
    @Accessor
    ResourceKey<Structure> getStructure();

    @Nullable
    @Accessor
    ResourceKey<Level> getDimension();

    @Nullable
    @Accessor
    Boolean getSmokey();

    @Accessor
    LightPredicate getLight();

    @Accessor
    BlockPredicate getBlock();

    @Accessor
    FluidPredicate getFluid();
}
