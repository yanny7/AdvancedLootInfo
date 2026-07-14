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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public class FakeChunkGenerator extends ChunkGenerator {
    private final ChunkGenerator realGenerator;

    public FakeChunkGenerator(ServerLevel serverLevel, Holder<Biome> forcedBiome) {
        super(new BiomeSource() {
            @NotNull
            @Override
            protected Codec<? extends BiomeSource> codec() {
                throw new UnsupportedOperationException();
            }

            @NotNull
            @Override
            protected Stream<Holder<Biome>> collectPossibleBiomes() {
                return Stream.of(forcedBiome);
            }

            @NotNull
            @Override
            public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
                return forcedBiome;
            }
        });
        this.realGenerator = serverLevel.getChunkSource().getGenerator();
    }

    @NotNull
    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {

    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {
        if (realGenerator instanceof NoiseBasedChunkGenerator generator) {
            generator.buildSurface(worldGenRegion, structureManager, randomState, chunkAccess);
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return realGenerator.getGenDepth();
    }

    @NotNull
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = realGenerator.getMinY(); y <= realGenerator.getSeaLevel(); y++) {
                    mutablePos.set(x, y, z);

                    if (realGenerator instanceof NoiseBasedChunkGenerator generator) {
                        chunkAccess.setBlockState(mutablePos, generator.generatorSettings().value().defaultBlock(), false);
                    } else {
                        chunkAccess.setBlockState(mutablePos, Blocks.STONE.defaultBlockState(), false);
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunkAccess);
    }

    @Override
    public int getSeaLevel() {
        return realGenerator.getSeaLevel();
    }

    @Override
    public int getMinY() {
        return realGenerator.getMinY();
    }

    @Override
    public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return realGenerator.getBaseHeight(i, j, types, levelHeightAccessor, randomState);
    }

    @NotNull
    @Override
    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return realGenerator.getBaseColumn(i, j, levelHeightAccessor, randomState);
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {

    }
}
