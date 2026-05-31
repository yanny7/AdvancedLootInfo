package com.yanny.awi.plugin.common.nodes;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public class FakeChunkGenerator extends ChunkGenerator {
    private final ServerLevel serverLevel;

    public FakeChunkGenerator(ServerLevel serverLevel) {
        super(new BiomeSource() {
            @NotNull
            @Override
            protected Codec<? extends BiomeSource> codec() {
                throw new IllegalStateException();
            }

            @NotNull
            @Override
            protected Stream<Holder<Biome>> collectPossibleBiomes() {
                return Stream.empty();
            }

            @NotNull
            @Override
            public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
                throw new IllegalStateException();
            }
        });
        this.serverLevel = serverLevel;
    }

    @NotNull
    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        throw new IllegalStateException();
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {

    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return serverLevel.getChunkSource().getGenerator().getGenDepth(); //TODO
    }

    @NotNull
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        throw new IllegalStateException();
    }

    @Override
    public int getSeaLevel() {
        return serverLevel.getSeaLevel();
    }

    @Override
    public int getMinY() {
        return serverLevel.getChunkSource().getGenerator().getMinY(); //TODO
    }

    @Override
    public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return serverLevel.getChunkSource().getGenerator().getBaseHeight(i, j, types, levelHeightAccessor, randomState); //TODO
    }

    @NotNull
    @Override
    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        throw new IllegalStateException();
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {

    }
}
