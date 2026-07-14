package com.yanny.awi.plugin.common.nodes;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NodeUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int[] TEST_SURFACE_DEPTHS = {4, 0, -4};
    private static final int[] TEST_ABOVE_LEVELS   = {1, 2};

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
                @Override
                public int getHeight() {
                    return settings.noiseSettings().height();
                }

                @Override
                public int getMinBuildHeight() {
                    return settings.noiseSettings().minY();
                }
            };

            this.minBuildHeight = heightAccessor.getMinBuildHeight();
            this.maxBuildHeight = heightAccessor.getMaxBuildHeight();
            this.seaLevel = noiseGenerator.getSeaLevel();

            ProtoChunk mockChunk = new ProtoChunk(new ChunkPos(0, 0), UpgradeData.EMPTY, heightAccessor, biomeRegistry, null);
            WorldGenerationContext genContext = new WorldGenerationContext(noiseGenerator, heightAccessor);
            BlockState defaultFluid = settings.defaultFluid();

            NoiseChunk dummyNoiseChunk = NoiseChunk.forChunk(
                    mockChunk, randomState,
                    DensityFunctions.BeardifierMarker.INSTANCE,
                    settings,
                    (i, j, k) -> new Aquifer.FluidStatus(seaLevel, defaultFluid),
                    Blender.empty()
            );

            this.context = new SurfaceRules.Context(
                    randomState.surfaceSystem(), randomState, mockChunk,
                    dummyNoiseChunk, biomeWrapper, biomeRegistry, genContext
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

    static final int maxTestDepth  = 16;
    static final int maxSameReject = 8;

    public static class Range {
        int min, max;

        public Range(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public String toString() {
            return (min == max) ? String.valueOf(min) : min + ".." + max;
        }
    }

    public static class RangeHolder {
        private final Set<Integer> yLevels = new HashSet<>();

        public boolean add(int position) {
            return yLevels.add(position);
        }

        public List<Range> buildRanges() {
            if (yLevels.isEmpty()) {
                return Collections.emptyList();
            }

            List<Integer> sortedPositions = new ArrayList<>(yLevels);

            Collections.sort(sortedPositions);

            List<Range> compiledRanges = new ArrayList<>();
            int start = sortedPositions.get(0);
            int end = start;

            for (int i = 1; i < sortedPositions.size(); i++) {
                int current = sortedPositions.get(i);

                if (current != end + 1) {
                    compiledRanges.add(new Range(start, end));
                    start = current;
                }

                end = current;
            }

            compiledRanges.add(new Range(start, end));
            return compiledRanges;
        }
    }

    public static class LayerHolder {
        Map<Block, RangeHolder> blocks = new HashMap<>();

        public boolean add(Block block, int y) {
            return blocks.computeIfAbsent(block, k -> new RangeHolder()).add(y);
        }

        public void log() {
            if (blocks.isEmpty()) {
                LOGGER.info("   -> No surface blocks discovered for this biome.");
                return;
            }

            List<Block> sortedBlocks = new ArrayList<>(blocks.keySet());
            sortedBlocks.sort(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey(b).toString()));

            for (Block block : sortedBlocks) {
                String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
                List<Range> rangesList = blocks.get(block).buildRanges();
                String rangesString = rangesList.stream()
                        .map(Range::toString)
                        .collect(Collectors.joining(", ", "[", "]"));

                LOGGER.info(" * {} -> Y levels: {}", blockId, rangesString);
            }
        }
    }

    private static void iterateSpiralChunks(BiPredicate<Integer, Integer> consumer) {
        int x = 0, z = 0, dx = 0, dz = -1;
        int step = 1, stepCount = 0, turnCount = 0;
        boolean first = true;

        while (true) {
            if (first) {
                first = false;
            } else {
                x += dx;
                z += dz;
                stepCount++;

                if (stepCount == step) {
                    int tmp = dx;

                    stepCount = 0;
                    dx = -dz;
                    dz = tmp;
                    turnCount++;

                    if (turnCount % 2 == 0) {
                        step++;
                    }
                }
                if (!consumer.test(x, z)) {
                    return;
                }
            }
        }
    }

    private static boolean scanEdges(DimensionContext dimCtx, LayerHolder blocks, int chunkX, int chunkZ) {
        boolean anyChange = false;

        int posX = chunkX * 16 + 8;
        int posZ = chunkZ * 16 + 8;
        int seaLevel = dimCtx.seaLevel;
        int minH = dimCtx.minBuildHeight;
        int maxH = dimCtx.maxBuildHeight;

        SurfaceRules.Context context  = dimCtx.context;
        SurfaceRules.SurfaceRule compiledRule = dimCtx.compiledRule;
        Map<Block, List<Integer>> pendingChanges = new HashMap<>();
        Map<Integer, Set<Block>> mins = new HashMap<>();
        Map<Integer, Set<Block>> maxs = new HashMap<>();


        for (Map.Entry<Block, RangeHolder> entry : blocks.blocks.entrySet()) {
            Block expectedBlock = entry.getKey();

            for (Range range : entry.getValue().buildRanges()) {
                mins.computeIfAbsent(range.min, k -> new HashSet<>()).add(expectedBlock);
                maxs.computeIfAbsent(range.max, k -> new HashSet<>()).add(expectedBlock);
            }
        }


        for (Map.Entry<Integer, Set<Block>> entry : mins.entrySet()) {
            int min = entry.getKey();
            Set<Block> expectedBlocks = entry.getValue();

            for (int y = min; y >= minH; y--) {
                Set<Block> foundBlocks = queryBlock(context, compiledRule, posX, posZ, y, seaLevel);

                if (foundBlocks.isEmpty()) {
                    break;
                }

                boolean foundNewBlock = false;

                for (Block found : foundBlocks) {
                    if (!blocks.blocks.containsKey(found)) {
                        foundNewBlock = true;
                    }
                    if (y < min || !expectedBlocks.contains(found)) {
                        pendingChanges.computeIfAbsent(found, k -> new ArrayList<>()).add(y);
                    }
                }

                if (foundNewBlock) {
                    return fullScan(context, compiledRule, blocks, posX, posZ, seaLevel, minH, maxH);
                }

                if (Collections.disjoint(foundBlocks, expectedBlocks)) {
                    break;
                }
            }
        }

        for (Map.Entry<Integer, Set<Block>> entry : maxs.entrySet()) {
            int max = entry.getKey();
            Set<Block> expectedBlocks = entry.getValue();

            for (int y = max; y <= maxH; y++) {
                Set<Block> foundBlocks = queryBlock(context, compiledRule, posX, posZ, y, seaLevel);

                if (foundBlocks.isEmpty()) {
                    break;
                }

                boolean foundNewBlock = false;

                for (Block found : foundBlocks) {
                    if (!blocks.blocks.containsKey(found)) {
                        foundNewBlock = true;
                    }
                    if (y > max || !expectedBlocks.contains(found)) {
                        pendingChanges.computeIfAbsent(found, k -> new ArrayList<>()).add(y);
                    }
                }

                if (foundNewBlock) {
                    return fullScan(context, compiledRule, blocks, posX, posZ, seaLevel, minH, maxH);
                }

                if (Collections.disjoint(foundBlocks, expectedBlocks)) {
                    break;
                }
            }
        }

        for (Map.Entry<Block, List<Integer>> change : pendingChanges.entrySet()) {
            for (int y : change.getValue()) {
                if (blocks.add(change.getKey(), y)) {
                    anyChange = true;
                }
            }
        }

        return anyChange;
    }

    private static boolean fullScan(SurfaceRules.Context context, SurfaceRules.SurfaceRule compiledRule, LayerHolder blocks,
                                    int posX, int posZ, int seaLevel, int minH, int maxH) {
        boolean anyChange = false;
        BlockState lastState = null;
        int[] fluidLevel;

        context.updateXZ(posX, posZ);

        for (int y = maxH; y >= minH; y--) {
            int[] minSurfaceLevels = new int[]{y - 1, y + 1};

            if (y <= seaLevel) {
                fluidLevel = new int[]{Integer.MIN_VALUE, seaLevel};
            } else {
                fluidLevel = new int[]{seaLevel};
            }

            for (int above : TEST_ABOVE_LEVELS) {
                for (int level : fluidLevel) {
                    for (int surfaceDepth : TEST_SURFACE_DEPTHS) {
                        int consecutiveSame = 0;

                        for (int minSurfaceLevel : minSurfaceLevels) {
                            for (int depth = 0; depth <= maxTestDepth; depth++) {
                                context.lastMinSurfaceLevelUpdate = ++context.lastUpdateXZ;
                                context.surfaceDepth = surfaceDepth;
                                context.minSurfaceLevel = minSurfaceLevel;
                                context.updateY(depth, above, level, posX, y, posZ);

                                BlockState cur = compiledRule.tryApply(posX, y, posZ);

                                if (Objects.equals(cur, lastState)) {
                                    if (++consecutiveSame >= maxSameReject) break;
                                } else {
                                    consecutiveSame = 0;
                                    lastState = cur;

                                    if (cur != null && blocks.add(cur.getBlock(), y)) {
                                        anyChange = true;
                                    }
                                }
                            }

                            consecutiveSame = 0;

                            for (int depth = 0; depth <= maxTestDepth; depth++) {
                                context.lastMinSurfaceLevelUpdate = ++context.lastUpdateXZ;
                                context.surfaceDepth = surfaceDepth;
                                context.minSurfaceLevel = minSurfaceLevel;
                                context.updateY(above, depth, level, posX, y, posZ);

                                BlockState cur = compiledRule.tryApply(posX, y, posZ);

                                if (Objects.equals(cur, lastState)) {
                                    if (++consecutiveSame >= maxSameReject) break;
                                } else {
                                    consecutiveSame = 0;
                                    lastState = cur;

                                    if (cur != null && blocks.add(cur.getBlock(), y)) {
                                        anyChange = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return anyChange;
    }

    @NotNull
    private static Set<Block> queryBlock(SurfaceRules.Context context, SurfaceRules.SurfaceRule compiledRule, int posX, int posZ, int y, int seaLevel) {
        context.updateXZ(posX, posZ);

        Set<Block> foundBlocks = new HashSet<>();
        int[] fluidLevel;
        int[] minSurfaceLevels = new int[]{y - 1, y + 1};

        if (y <= seaLevel) {
            fluidLevel = new int[]{Integer.MIN_VALUE, seaLevel};
        } else {
            fluidLevel = new int[]{seaLevel};
        }

        for (int above : TEST_ABOVE_LEVELS) {
            for (int level : fluidLevel) {
                for (int surfaceDepth : TEST_SURFACE_DEPTHS) {
                    for (int minSurfaceLevel : minSurfaceLevels) {
                        for (int depth = 0; depth <= maxTestDepth; depth++) {
                            context.lastMinSurfaceLevelUpdate = ++context.lastUpdateXZ;
                            context.surfaceDepth = surfaceDepth;
                            context.minSurfaceLevel = minSurfaceLevel;
                            context.updateY(depth, above, level, posX, y, posZ);

                            BlockState cur = compiledRule.tryApply(posX, y, posZ);

                            if (cur != null) {
                                foundBlocks.add(cur.getBlock());
                            }
                        }

                        for (int depth = 0; depth <= maxTestDepth; depth++) {
                            context.lastMinSurfaceLevelUpdate = ++context.lastUpdateXZ;
                            context.surfaceDepth = surfaceDepth;
                            context.minSurfaceLevel = minSurfaceLevel;
                            context.updateY(above, depth, level, posX, y, posZ);

                            BlockState cur = compiledRule.tryApply(posX, y, posZ);

                            if (cur != null) {
                                foundBlocks.add(cur.getBlock());
                            }
                        }
                    }
                }
            }
        }
        return foundBlocks;
    }

    @NotNull
    public static LayerHolder getBaseBlocksForBiome(DimensionContext dimCtx, Holder<Biome> targetBiome) {
        LayerHolder discoveredBlocks = new LayerHolder();

        try {
            int[] noChangeCount = {0};

            dimCtx.biomeWrapper.currentBiome = targetBiome;
            fullScan(dimCtx.context, dimCtx.compiledRule, discoveredBlocks, 8, 8, dimCtx.seaLevel, dimCtx.minBuildHeight, dimCtx.maxBuildHeight);

            iterateSpiralChunks((cx, cz) -> {
                if (scanEdges(dimCtx, discoveredBlocks, cx, cz)) {
                    noChangeCount[0] = 0;
                } else {
                    return ++noChangeCount[0] < maxSameReject * 8;
                }

                return true;
            });

        } catch (Throwable ignored) {}

        return discoveredBlocks;
    }
}