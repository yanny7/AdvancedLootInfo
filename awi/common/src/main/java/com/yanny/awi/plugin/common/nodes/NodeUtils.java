package com.yanny.awi.plugin.common.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class NodeUtils {
    @NotNull
    public static Set<Block> mineSurfaceBlocksForBiome(RegistryAccess registryAccess, NoiseBasedChunkGenerator noiseGenerator, RandomState randomState, Holder<Biome> targetBiome) {
        Set<Block> discoveredBlocks = new HashSet<>();
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
        SurfaceRules.RuleSource masterSurfaceRule = noiseGenerator.generatorSettings().value().surfaceRule();
        NoiseGeneratorSettings settings = noiseGenerator.generatorSettings().value();
        LevelHeightAccessor heightAccessor = new LevelHeightAccessor() {
            @Override
            public int getHeight() {
                return settings.noiseSettings().height();
            }

            @Override
            public int getMinBuildHeight() {
                return settings.noiseSettings().minY();
            }
        };
        int minBuildHeight = heightAccessor.getMinBuildHeight();
        int maxBuildHeight = heightAccessor.getMaxBuildHeight();
        ChunkPos samplePos = new ChunkPos(0, 0);
        ProtoChunk mockChunk = new ProtoChunk(samplePos, UpgradeData.EMPTY, heightAccessor, biomeRegistry, null);
        Function<BlockPos, Holder<Biome>> mockedBiomeGetter = (blockPos) -> targetBiome;
        WorldGenerationContext genContext = new WorldGenerationContext(noiseGenerator, heightAccessor);
        NoiseChunk dummyNoiseChunk = NoiseChunk.forChunk(
                mockChunk,
                randomState,
                DensityFunctions.BeardifierMarker.INSTANCE,
                noiseGenerator.generatorSettings().value(),
                (i, j, k) -> new Aquifer.FluidStatus(0, Fluids.EMPTY.defaultFluidState().createLegacyBlock()),
                Blender.empty()
        );

        try {
            SurfaceRules.Context context = new SurfaceRules.Context(
                    randomState.surfaceSystem(),
                    randomState,
                    mockChunk,
                    dummyNoiseChunk,
                    mockedBiomeGetter,
                    biomeRegistry,
                    genContext
            );

            int blockX = 8;
            int blockZ = 8;

            context.updateXZ(blockX, blockZ);

            SurfaceRules.SurfaceRule compiledRule = masterSurfaceRule.apply(context);

            if (compiledRule != null) {
                int seaLevel = noiseGenerator.getSeaLevel();

                for (int y = maxBuildHeight; y >= minBuildHeight; y--) {
                    context.updateY(0, 0,seaLevel, blockX, y, blockZ);
                    context.stoneDepthAbove = 0;
                    context.stoneDepthBelow = 5;

                    BlockState evaluatedState = compiledRule.tryApply(blockX, y, blockZ);

                    if (evaluatedState != null && !evaluatedState.isAir()) {
                        discoveredBlocks.add(evaluatedState.getBlock());
                    }

                    context.updateY(3, 0, seaLevel, blockX, y , blockZ);
                    context.stoneDepthAbove = 3;
                    context.stoneDepthBelow = 2;
                    evaluatedState = compiledRule.tryApply(blockX, y, blockZ);

                    if (evaluatedState != null && !evaluatedState.isAir()) {
                        discoveredBlocks.add(evaluatedState.getBlock());
                    }
                }
            }

        } catch (Throwable ignored) {
        }

        return discoveredBlocks;
    }
}
