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
        private final int totalHeight;
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
            this.totalHeight = heightAccessor.getHeight();
            this.seaLevel = noiseGenerator.getSeaLevel();

            ProtoChunk mockChunk = new ProtoChunk(new ChunkPos(0, 0), UpgradeData.EMPTY, heightAccessor, biomeRegistry, null);
            WorldGenerationContext genContext = new WorldGenerationContext(noiseGenerator, heightAccessor);
            BlockState defaultFluid = settings.defaultFluid();

            NoiseChunk dummyNoiseChunk = NoiseChunk.forChunk(
                    mockChunk,
                    randomState,
                    DensityFunctions.BeardifierMarker.INSTANCE,
                    settings,
                    (i, j, k) -> new Aquifer.FluidStatus(seaLevel, defaultFluid),
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

            int seaLevel = dimCtx.seaLevel;
            int minH = dimCtx.minBuildHeight;
            int maxH = dimCtx.maxBuildHeight;
            int totalH = dimCtx.totalHeight;
            int initialScanRadius = Math.max(4, totalH / 64);
            int baseMaxStep = Math.max(2, totalH / 64);
            int totalChunksScanned = 0;
            int lastDiscoveryChunkCount = 0;

            for (int cx = -initialScanRadius; cx < initialScanRadius; cx++) {
                for (int cz = -initialScanRadius; cz < initialScanRadius; cz++) {
                    totalChunksScanned++;

                    if (totalChunksScanned > 32 && (totalChunksScanned - lastDiscoveryChunkCount) > (totalChunksScanned / 2)) {
                        if ((cx + cz) % 2 != 0) continue;
                    }

                    int xOffset = 2 + (Math.abs(cx * 881) % 12);
                    int zOffset = 2 + (Math.abs(cz * 919) % 12);
                    int blockX = (cx << 4) + xOffset;
                    int blockZ = (cz << 4) + zOffset;

                    context.updateXZ(blockX, blockZ);

                    int heightVarianceRange = totalH / 5;
                    int waveFactor = ((cx ^ cz) * 31) % (heightVarianceRange | 1);
                    int simulatedSurfaceY = seaLevel + (heightVarianceRange / 2) - Math.abs(waveFactor);

                    simulatedSurfaceY = Math.min(Math.max(simulatedSurfaceY, minH + 16), maxH - 16);

                    boolean isCoreZone = (Math.abs(cx) <= 1 && Math.abs(cz) <= 1);
                    int maxVerticalStep = isCoreZone ? 1 : baseMaxStep;
                    int previousDiscoveredSize = discoveredBlocks.size();
                    int currentStep = 1;
                    int y = maxH;

                    while (y >= minH) {
                        int depthBelow = Math.max(0, simulatedSurfaceY - y);
                        int depthAbove = Math.max(0, y - simulatedSurfaceY);
                        context.updateY(depthAbove, depthBelow, seaLevel, blockX, y, blockZ);
                        context.stoneDepthAbove = depthAbove;
                        context.stoneDepthBelow = depthBelow;
                        BlockState stateCurrent = compiledRule.tryApply(blockX, y, blockZ);
                        Block blockCurrent = (stateCurrent != null && !stateCurrent.isAir()) ? stateCurrent.getBlock() : null;

                        if (blockCurrent != null) {
                            discoveredBlocks.add(blockCurrent);
                        }

                        if (y == minH) {
                            break;
                        }

                        int nextY = Math.max(minH, y - currentStep);
                        int nextDepthBelow = Math.max(0, simulatedSurfaceY - nextY);
                        int nextDepthAbove = Math.max(0, nextY - simulatedSurfaceY);

                        context.updateY(nextDepthAbove, nextDepthBelow, seaLevel, blockX, nextY, blockZ);
                        context.stoneDepthAbove = nextDepthAbove;
                        context.stoneDepthBelow = nextDepthBelow;

                        BlockState stateNext = compiledRule.tryApply(blockX, nextY, blockZ);
                        Block blockNext = (stateNext != null && !stateNext.isAir()) ? stateNext.getBlock() : null;

                        if (blockCurrent != blockNext || y == nextY) {
                            y--;
                            currentStep = 1;
                        } else {
                            y = nextY;
                            currentStep = Math.min(maxVerticalStep, currentStep + 1);
                        }
                    }

                    if (discoveredBlocks.size() > previousDiscoveredSize) {
                        lastDiscoveryChunkCount = totalChunksScanned;
                    }
                }
            }
        } catch (Throwable ignored) {}

        return discoveredBlocks;
    }
}