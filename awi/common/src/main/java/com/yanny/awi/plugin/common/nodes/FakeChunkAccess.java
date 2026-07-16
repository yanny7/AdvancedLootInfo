package com.yanny.awi.plugin.common.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.TickContainerAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FakeChunkAccess extends ChunkAccess {
    private final LevelChunkSection fakeChunkSection;
    private final Set<Block> blocks;

    public FakeChunkAccess(FakeWorldGenLevel level, ServerLevel serverLevel, Set<Block> blocks) {
        super(null, null, level, serverLevel.registryAccess().lookupOrThrow(Registries.BIOME), 0, null, null);
        fakeChunkSection = new LevelChunkSection(serverLevel.registryAccess().lookupOrThrow(Registries.BIOME)) {
            @NotNull
            @Override
            public BlockState getBlockState(int i, int j, int k) {
                int y = j;

                // 1. Give features an "Air" sky pocket to grow into
                if (y > 64) {
                    return Blocks.AIR.defaultBlockState();
                }

                // 2. Give vegetation/structures a valid surface to spawn on
                if (y == 64) {
                    // Return Grass/Dirt so trees, flowers, and wells think they are on the surface
                    return Blocks.GRASS_BLOCK.defaultBlockState();
                }

                // 3. Provide valid stone/deepslate layers for ore replacement checks
                if (y < 0) {
                    return Blocks.DEEPSLATE.defaultBlockState();
                }

                return Blocks.STONE.defaultBlockState();
//        return Blocks.STONE.defaultBlockState(); //TODO random pool?
            }

            @NotNull
            @Override
            public FluidState getFluidState(int i, int j, int k) {
                return Fluids.EMPTY.defaultFluidState(); //TODO
            }

            @Override
            public void acquire() {

            }
        };
        this.blocks = blocks;
    }

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos blockPos, BlockState blockState, int i) {
        blocks.add(blockState.getBlock());
        return blockState;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {

    }

    @Override
    public void addEntity(Entity entity) {

    }

    @NotNull
    @Override
    public ChunkStatus getPersistedStatus() {
        return null;
    }

    @Override
    public void removeBlockEntity(BlockPos blockPos) {

    }

    @Nullable
    @Override
    public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos, HolderLookup.Provider provider) {
        return null;
    }

    @NotNull
    @Override
    public TickContainerAccess<Block> getBlockTicks() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public TickContainerAccess<Fluid> getFluidTicks() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public PackedTicks getTicksForSerialization(long l) {
        throw new IllegalStateException();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return null;
    }

    @NotNull
    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        int y = blockPos.getY();

        // 1. Give features an "Air" sky pocket to grow into
        if (y > 64) {
            return Blocks.AIR.defaultBlockState();
        }

        // 2. Give vegetation/structures a valid surface to spawn on
        if (y == 64) {
            // Return Grass/Dirt so trees, flowers, and wells think they are on the surface
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }

        // 3. Provide valid stone/deepslate layers for ore replacement checks
        if (y < 0) {
            return Blocks.DEEPSLATE.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
//        return Blocks.STONE.defaultBlockState(); //TODO random pool?
    }

    @NotNull
    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return Fluids.EMPTY.defaultFluidState(); //TODO random pool
    }

    /*
     * REMOVED OVERRIDES
     */

    @Override
    public void markPosForPostprocessing(BlockPos blockPos) {

    }

    @NotNull
    @Override
    public LevelChunkSection getSection(int i) {
        return fakeChunkSection;
    }
}
