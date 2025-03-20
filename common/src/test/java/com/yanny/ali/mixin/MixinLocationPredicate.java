package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;

public interface MixinLocationPredicate {
    MinMaxBounds.Doubles getX();
    MinMaxBounds.Doubles getY();
    MinMaxBounds.Doubles getZ();

    @Nullable
    ResourceKey<Biome> getBiome();

    @Nullable
    ResourceKey<Structure> getStructure();

    @Nullable
    ResourceKey<Level> getDimension();

    @Nullable
    Boolean getSmokey();

    LightPredicate getLight();
    BlockPredicate getBlock();
    FluidPredicate getFluid();
}
