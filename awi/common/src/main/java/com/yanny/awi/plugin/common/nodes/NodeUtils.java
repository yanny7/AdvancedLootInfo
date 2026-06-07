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
    public static class DimensionContext {
        private final SurfaceRules.SurfaceRule compiledRule;
        private final SurfaceRules.Context context;
        private final int minBuildHeight;
        private final int maxBuildHeight;
        private final int seaLevel;
        private final BiomeHolderWrapper biomeWrapper = new BiomeHolderWrapper();

        public DimensionContext(RegistryAccess registryAccess, NoiseBasedChunkGenerator noiseGenerator, RandomState randomState) {
            Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
            NoiseGeneratorSettings settings = noiseGenerator.generatorSettings().value();
            SurfaceRules.RuleSource masterSurfaceRule = settings.surfaceRule();

            LevelHeightAccessor heightAccessor = new LevelHeightAccessor() {
                @Override public int getHeight() { return settings.noiseSettings().height(); }
                @Override public int getMinBuildHeight() { return settings.noiseSettings().minY(); }
            };

            this.minBuildHeight = heightAccessor.getMinBuildHeight();
            this.maxBuildHeight = heightAccessor.getMaxBuildHeight();
            this.seaLevel = noiseGenerator.getSeaLevel();

            ProtoChunk mockChunk = new ProtoChunk(new ChunkPos(0, 0), UpgradeData.EMPTY, heightAccessor, biomeRegistry, null);
            WorldGenerationContext genContext = new WorldGenerationContext(noiseGenerator, heightAccessor);

            NoiseChunk dummyNoiseChunk = NoiseChunk.forChunk(
                    mockChunk,
                    randomState,
                    DensityFunctions.BeardifierMarker.INSTANCE,
                    settings,
                    (i, j, k) -> new Aquifer.FluidStatus(0, Fluids.EMPTY.defaultFluidState().createLegacyBlock()),
                    Blender.empty()
            );

            this.context = new SurfaceRules.Context(
                    randomState.surfaceSystem(),
                    randomState,
                    mockChunk,
                    dummyNoiseChunk,
                    biomeWrapper,
                    biomeRegistry,
                    genContext
            );
            this.compiledRule = masterSurfaceRule.apply(this.context);
        }

        private static class BiomeHolderWrapper implements Function<BlockPos, Holder<Biome>> {
            public Holder<Biome> currentBiome;

            @Override
            public Holder<Biome> apply(BlockPos blockPos) {
                return currentBiome;
            }
        }
    }

    @NotNull
    public static Set<Block> getBaseBlocksForBiome(DimensionContext dimCtx, Holder<Biome> targetBiome) {
        Set<Block> discoveredBlocks = new HashSet<>();

        if (dimCtx.compiledRule == null) {
            return discoveredBlocks;
        }

        try {
            dimCtx.biomeWrapper.currentBiome = targetBiome;

            SurfaceRules.Context context = dimCtx.context;
            SurfaceRules.SurfaceRule compiledRule = dimCtx.compiledRule;

            int blockX = 8;
            int blockZ = 8;
            int seaLevel = dimCtx.seaLevel;

            context.updateXZ(blockX, blockZ);

            int minH = dimCtx.minBuildHeight;
            int maxH = dimCtx.maxBuildHeight;

            for (int y = maxH; y >= minH; y--) {
                context.updateY(0, 0, seaLevel, blockX, y, blockZ);
                context.stoneDepthAbove = 0;
                context.stoneDepthBelow = 5;

                BlockState evaluatedState = compiledRule.tryApply(blockX, y, blockZ);

                if (evaluatedState != null && !evaluatedState.isAir()) {
                    discoveredBlocks.add(evaluatedState.getBlock());
                }

                context.updateY(3, 0, seaLevel, blockX, y, blockZ);
                context.stoneDepthAbove = 3;
                context.stoneDepthBelow = 2;
                evaluatedState = compiledRule.tryApply(blockX, y, blockZ);

                if (evaluatedState != null && !evaluatedState.isAir()) {
                    discoveredBlocks.add(evaluatedState.getBlock());
                }
            }
        } catch (Throwable ignored) {}

        return discoveredBlocks;
    }
}