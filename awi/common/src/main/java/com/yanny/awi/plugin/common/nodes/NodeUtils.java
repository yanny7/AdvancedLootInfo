package com.yanny.awi.plugin.common.nodes;

import com.mojang.logging.LogUtils;
import com.yanny.aci.api.RangeValue;
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
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;

/**
 * Determines, per biome, which blocks the dimension's surface rules place and at what vertical position.
 * <p>
 * Instead of fuzzing the {@link SurfaceRules.Context} inputs across a fixed grid of guessed values, this
 * evaluates the (mod-agnostic) compiled surface rule against a <b>physically consistent, canonical column</b>:
 * flat terrain with the surface at an assumed height {@code H}, solid below, water up to sea level, air above.
 * All rule inputs ({@code stoneDepthAbove/Below}, {@code waterHeight}, {@code minSurfaceLevel}) are then
 * <i>derived</i> from that column exactly like {@code SurfaceSystem#buildSurface} does — no magic constants.
 * <p>
 * The assumed surface height {@code H} is swept across the world's build range (its only bound) and a handful of
 * horizontal sample points feed the real 2D surface/noise-threshold fields. Every rule hit is recorded both as an
 * absolute Y and as a depth below the assumed surface; a block is then classified empirically:
 * <ul>
 *     <li><b>surface-relative</b> (grass, dirt, sand, badlands bands) — its depth below the surface is stable while
 *     its absolute Y tracks {@code H}; reported as "depth below surface".</li>
 *     <li><b>absolute</b> (deepslate, bedrock) — its absolute Y is stable while its depth tracks {@code H}; reported
 *     as an absolute Y range.</li>
 * </ul>
 */
public class NodeUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Sampling knobs (performance/coverage trade-off, NOT rule-logic magic values).
    // The scan converges: each round samples a fresh batch of horizontal points and a shifted surface-height phase;
    // it repeats while rounds keep discovering new (block, position) observations and stops once a full phase cycle
    // adds nothing. This lets simple dimensions finish in a couple rounds while volumetric ones densify themselves.
    private static final int COLUMNS_PER_ROUND = 8;
    private static final int SURFACE_HEIGHT_STEP = 4;
    // Stop after this many consecutive rounds add nothing new. A full phase cycle (== step) guarantees every surface
    // height was retried before concluding coverage is complete.
    private static final int STABLE_ROUNDS = SURFACE_HEIGHT_STEP;
    // Hard safety cap so a pathological rule cannot loop forever; hitting it is logged, never silently truncated.
    private static final int MAX_ROUNDS = 40;
    // How far below the preliminary surface vanilla builds surface (SurfaceRules.Context constant), used to derive minSurfaceLevel.
    private static final int SURFACE_BUILD_DEPTH = 8;
    // Besides the solid column, each surface height is also probed as a thin floating stone slab (air below it) so that
    // ceiling-gated rules fire: ON_CEILING (stoneDepthBelow<=1, e.g. badlands red_sandstone) and UNDER_CEILING
    // (stoneDepthBelow<=1+surfaceDepth). Slab thickness is swept 1..this to cover that window.
    private static final int MAX_CEILING_THICKNESS = 8;

    public static class DimensionContext {
        private final SurfaceRules.SurfaceRule compiledRule;
        private final SurfaceRules.Context context;
        private final int minBuildHeight;
        private final int maxBuildHeight;
        private final int seaLevel;
        private final BlockState defaultBlock;
        private final BlockState defaultFluid;
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
            this.defaultBlock = settings.defaultBlock();
            this.defaultFluid = settings.defaultFluid();

            ProtoChunk mockChunk = new ProtoChunk(new ChunkPos(0, 0), UpgradeData.EMPTY, heightAccessor, biomeRegistry, null);
            WorldGenerationContext genContext = new WorldGenerationContext(noiseGenerator, heightAccessor);

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

    /** Collects a set of integer positions and compacts them into contiguous ranges. */
    public static class RangeHolder {
        private final Set<Integer> positions = new HashSet<>();

        public boolean add(int position) {
            return positions.add(position);
        }

        public int size() {
            return positions.size();
        }

        /** Number of disjoint contiguous ranges the recorded positions collapse into (1 == a single solid band). */
        public int clusterCount() {
            return buildRanges().size();
        }

        /** Span between the lowest and highest recorded position (0 when empty or single-valued). */
        public int spread() {
            if (positions.isEmpty()) {
                return 0;
            }

            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;

            for (int p : positions) {
                min = Math.min(min, p);
                max = Math.max(max, p);
            }

            return max - min;
        }

        public List<RangeValue> buildRanges() {
            if (positions.isEmpty()) {
                return Collections.emptyList();
            }

            List<Integer> sorted = new ArrayList<>(positions);

            Collections.sort(sorted);

            List<RangeValue> ranges = new ArrayList<>();
            int start = sorted.get(0);
            int end = start;

            for (int i = 1; i < sorted.size(); i++) {
                int current = sorted.get(i);

                if (current != end + 1) {
                    ranges.add(new RangeValue(start, end));
                    start = current;
                }

                end = current;
            }

            ranges.add(new RangeValue(start, end));
            return ranges;
        }
    }

    /** Per-block observations: every rule hit is recorded both by depth-below-surface and by absolute Y. */
    private static class BlockObservation {
        // A surface-relative block whose absolute Y fragments into at least this many disjoint bands is reported as
        // "layered" rather than "depth below surface": its identity is a periodic function of absolute Y (e.g. the
        // badlands banded-terracotta strata, clay bands mod 192) so a single depth range would be actively misleading.
        // This is a reporting/classification threshold, not rule logic — observed banded blocks fragment into 5..28
        // bands while plain surface layers stay at a single contiguous band, so the boundary is wide.
        private static final int LAYERED_MIN_BANDS = 3;

        final RangeHolder depths = new RangeHolder();
        final RangeHolder absolute = new RangeHolder();
        // Depths split by placement context: floor = normal below-surface placement (sandstone under the sand),
        // ceiling = ON_CEILING overhang placement (red_sandstone). Kept apart because merging them pollutes the
        // reported "depth below surface" — an overhang exposes a block at depth 0 that is really several blocks down.
        final RangeHolder floorDepths = new RangeHolder();
        final RangeHolder ceilingDepths = new RangeHolder();
        // Which flooding contexts the rule placed this block in. Set from whether the walk had water above the surface:
        // ocean-floor blocks (sandstone, gravel) only fire when the underwater rule branches see a water column,
        // dry-land blocks only fire without one, and depth blocks (deepslate, bedrock) appear either way.
        private boolean seenUnderwater;
        private boolean seenDry;

        void record(int depthBelowSurface, int absoluteY, boolean underwater, boolean ceiling) {
            depths.add(depthBelowSurface);
            absolute.add(absoluteY);
            (ceiling ? ceilingDepths : floorDepths).add(depthBelowSurface);

            if (underwater) {
                seenUnderwater = true;
            } else {
                seenDry = true;
            }
        }

        /** Whether this block requires water above it, dry land above it, or occurs regardless of the water level. */
        WaterConstraint waterConstraint() {
            if (seenUnderwater && seenDry) {
                return WaterConstraint.ANY;
            }

            return seenUnderwater ? WaterConstraint.UNDERWATER : WaterConstraint.DRY;
        }

        /**
         * Whether this block is a normal below-surface (floor) placement, an overhang-only (ceiling) placement such as
         * badlands red_sandstone, or occurs both ways. Ceiling-only blocks are the ones the "depth below surface"
         * framing does not fit — they only exist on the underside of an overhang.
         */
        Placement placement() {
            boolean floor = floorDepths.size() > 0;
            boolean ceiling = ceilingDepths.size() > 0;

            if (floor && ceiling) {
                return Placement.ANY;
            }

            return ceiling ? Placement.CEILING : Placement.FLOOR;
        }

        /** Depths to report for a surface-relative block: the floor placement, since merging in the overhang exposure
         *  (always at depth 0) would wrongly claim a normally-buried block reaches the surface. Ceiling-only blocks
         *  have no floor placement, so their ceiling depth is used instead. */
        RangeHolder reportedDepths() {
            return floorDepths.size() > 0 ? floorDepths : ceilingDepths;
        }

        Kind classify() {
            // Surface-relative: depth-below-surface stays tighter than absolute Y as the assumed surface height is
            // swept (grass/sand track the surface), whereas absolute features (deepslate, bedrock) and volumetric
            // fills (netherrack) keep a smaller — or no smaller — absolute span.
            boolean surfaceRelative = depths.spread() < absolute.spread();

            if (!surfaceRelative) {
                return Kind.ABSOLUTE;
            }
            // Layered strata recur at many separated absolute-Y bands AND span a depth window at least as thick as the
            // surface-height sampling step. The thickness guard is essential and non-arbitrary: a block thinner than
            // the height step is only ever recorded once per swept surface height, so its absolute Y fragments into a
            // regular grid (spacing == the step) that mimics banding — a sampling artifact, not strata (a one-block
            // ice skin on frozen peaks, a lava-sea top). Only when the depth window bridges the step do consecutive
            // surface heights overlap, making the absolute-Y clustering a real property of the rule rather than the grid.
            if (depths.spread() >= SURFACE_HEIGHT_STEP && absolute.clusterCount() >= LAYERED_MIN_BANDS) {
                return Kind.LAYERED;
            }

            return Kind.SURFACE;
        }
    }

    private enum Kind { SURFACE, ABSOLUTE, LAYERED }

    /**
     * How a block's vertical positions are stored/reported.
     * <ul>
     *     <li>{@link #RELATIVE} — depth below the surface (grass, dirt, sand); {@link BlockInfo#ranges} are depths.</li>
     *     <li>{@link #ABSOLUTE} — a stable absolute Y band (deepslate, bedrock); {@link BlockInfo#ranges} are absolute Ys.</li>
     *     <li>{@link #LAYERED} — recurring absolute-Y strata (badlands bands); {@link BlockInfo#ranges} are absolute Ys.</li>
     * </ul>
     */
    public enum StorageType { RELATIVE, ABSOLUTE, LAYERED }

    /** Whether a placed block needs water above it, dry land above it, or is indifferent to the water level. */
    public enum WaterConstraint { UNDERWATER, DRY, ANY }

    /** Whether a block is a normal below-surface placement, an overhang/ceiling-only placement, or occurs both ways. */
    public enum Placement { FLOOR, CEILING, ANY }

    /**
     * Structured result for a single surface block: the block itself, how its positions are stored
     * ({@link StorageType}), the value {@link RangeValue}s in that storage's units, its {@link WaterConstraint}, and
     * its {@link Placement} (floor vs. ceiling/overhang).
     */
    public record BlockInfo(Block block, StorageType storageType, List<RangeValue> ranges, WaterConstraint water, Placement placement) {}

    public static class LayerHolder {
        private final Map<Block, BlockObservation> blocks = new HashMap<>();

        public boolean isEmpty() {
            return blocks.isEmpty();
        }

        void record(Block block, int assumedSurface, int y, boolean underwater, boolean ceiling) {
            blocks.computeIfAbsent(block, k -> new BlockObservation()).record(assumedSurface - y, y, underwater, ceiling);
        }

        /** Total number of distinct (block, depth) and (block, absoluteY) observations collected so far. */
        int observationCount() {
            int total = 0;

            for (BlockObservation obs : blocks.values()) {
                total += obs.depths.size() + obs.absolute.size();
            }

            return total;
        }

        /**
         * Snapshots every discovered block as a {@link BlockInfo}: its storage type (surface-relative depth vs.
         * absolute Y vs. layered strata), the corresponding value ranges, and whether it needs water above it.
         * Replaces the old per-block logging — the analysis states now flow to the client as structured data.
         */
        public Set<BlockInfo> getBlockInfos() {
            Set<BlockInfo> infos = new HashSet<>();

            for (Map.Entry<Block, BlockObservation> entry : blocks.entrySet()) {
                BlockObservation obs = entry.getValue();
                StorageType type;
                List<RangeValue> ranges;

                switch (obs.classify()) {
                    case SURFACE -> {
                        type = StorageType.RELATIVE;
                        ranges = obs.reportedDepths().buildRanges();
                    }
                    case ABSOLUTE -> {
                        type = StorageType.ABSOLUTE;
                        ranges = obs.absolute.buildRanges();
                    }
                    case LAYERED -> {
                        type = StorageType.LAYERED;
                        ranges = obs.absolute.buildRanges();
                    }
                    default -> throw new IllegalStateException("Unknown kind for block " + entry.getKey());
                }

                infos.add(new BlockInfo(entry.getKey(), type, ranges, obs.waterConstraint(), obs.placement()));
            }

            return infos;
        }
    }

    /**
     * Walks a single canonical column, mirroring {@code SurfaceSystem#buildSurface}: iterate top-down over a
     * contiguous stone run {@code [stoneBottom, surfaceTop]}, derive the real rule inputs from the column shape and
     * record every block the surface rule places.
     * <p>
     * The stone run's bottom is explicit so the same walk models two shapes:
     * <ul>
     *     <li>a normal surface — {@code stoneBottom} at world bottom, optional water above (large {@code stoneDepthBelow});</li>
     *     <li>a thin floating slab / overhang — {@code stoneBottom} just below the surface, air below it, which drives
     *     {@code stoneDepthBelow} down to 1 and fires ceiling-gated rules.</li>
     * </ul>
     */
    private static void walkColumn(DimensionContext dimCtx, LayerHolder holder, int posX, int posZ,
                                   int surfaceTop, int stoneBottom, boolean hasWaterAbove) {
        SurfaceRules.Context context = dimCtx.context;
        SurfaceRules.SurfaceRule rule = dimCtx.compiledRule;
        int seaLevel = dimCtx.seaLevel;

        context.updateXZ(posX, posZ);

        // Pin the preliminary surface to the assumed surface height so that surface-relative conditions
        // (above_preliminary_surface) resolve against it, exactly as vanilla derives minSurfaceLevel.
        context.minSurfaceLevel = surfaceTop + context.surfaceDepth - SURFACE_BUILD_DEPTH;
        context.lastMinSurfaceLevelUpdate = context.lastUpdateXZ;

        boolean water = hasWaterAbove && surfaceTop < seaLevel;
        int top = water ? seaLevel : surfaceTop;

        int stoneDepthAbove = 0;
        int waterHeight = Integer.MIN_VALUE;

        for (int y = top; y >= stoneBottom; y--) {
            if (y > surfaceTop) {
                // above the solid surface: water down to sea level (if any), else air
                if (water && y <= seaLevel) {
                    if (waterHeight == Integer.MIN_VALUE) {
                        waterHeight = y + 1;
                    }
                } else {
                    stoneDepthAbove = 0;
                    waterHeight = Integer.MIN_VALUE;
                }
                continue;
            }

            stoneDepthAbove++;
            int stoneDepthBelow = y - stoneBottom + 1;
            context.updateY(stoneDepthAbove, stoneDepthBelow, waterHeight, posX, y, posZ);

            BlockState result = rule.tryApply(posX, y, posZ);

            if (result != null && !result.isAir() && dimCtx.defaultFluid != result && dimCtx.defaultBlock != result) {
                // A block placed at the underside of the stone run (stoneDepthBelow<=1, ON_CEILING) is an overhang/
                // ceiling placement (badlands red_sandstone), distinct from a normal below-surface floor placement.
                boolean ceiling = stoneDepthBelow <= 1;
                holder.record(result.getBlock(), surfaceTop, y, water, ceiling);
            }
        }
    }

    @NotNull
    public static LayerHolder getBaseBlocksForBiome(DimensionContext dimCtx, Holder<Biome> targetBiome) {
        LayerHolder discoveredBlocks = new LayerHolder();
        long start = System.currentTimeMillis();
        int round = 0;
        int walks = 0;
        int stableRounds = 0;

        try {
            dimCtx.biomeWrapper.currentBiome = targetBiome;

            // Repeat until a full phase cycle discovers nothing new (or the safety cap is hit).
            while (stableRounds < STABLE_ROUNDS && round < MAX_ROUNDS) {
                int before = discoveredBlocks.observationCount();
                // Fresh horizontal points every round (new 2D noise values) and a shifted surface-height phase so that,
                // over a full cycle, every possible surface height is retried (fills absolute-Y gaps in volumetric dims).
                int heightPhase = round % SURFACE_HEIGHT_STEP;
                List<long[]> samples = sampleColumns(round * COLUMNS_PER_ROUND);

                for (long[] xz : samples) {
                    int posX = (int) xz[0];
                    int posZ = (int) xz[1];

                    for (int h = dimCtx.maxBuildHeight - 1 - heightPhase; h >= dimCtx.minBuildHeight; h -= SURFACE_HEIGHT_STEP) {
                        // Normal surface: solid stone from the world bottom up to h.
                        walkColumn(dimCtx, discoveredBlocks, posX, posZ, h, dimCtx.minBuildHeight, true);
                        walks++;

                        // Overhang surfaces: thin floating slabs so ceiling-gated rules fire. Dry slabs cover land
                        // overhangs (badlands red_sandstone); below sea level a water-covered slab is also probed so
                        // underwater ceiling rules fire (lukewarm-ocean sandstone), which a dry column never reaches.
                        for (int thickness = 1; thickness <= MAX_CEILING_THICKNESS; thickness++) {
                            int stoneBottom = h - thickness + 1;

                            if (stoneBottom < dimCtx.minBuildHeight) {
                                break;
                            }

                            walkColumn(dimCtx, discoveredBlocks, posX, posZ, h, stoneBottom, false);
                            walks++;

                            if (h < dimCtx.seaLevel) {
                                walkColumn(dimCtx, discoveredBlocks, posX, posZ, h, stoneBottom, true);
                                walks++;
                            }
                        }
                    }
                }

                stableRounds = (discoveredBlocks.observationCount() == before) ? stableRounds + 1 : 0;
                round++;
            }
        } catch (Throwable t) {
            LOGGER.warn("Surface scan failed for biome {}", targetBiome.unwrapKey().map(Object::toString).orElse("?"), t);
        }

        if (round >= MAX_ROUNDS) {
            LOGGER.warn("Surface scan for biome {} hit the {}-round cap; coverage may be incomplete",
                    targetBiome.unwrapKey().map(Object::toString).orElse("?"), MAX_ROUNDS);
        }

        LOGGER.info("   scan {}: {} rounds, {} walks, {} blocks in {}ms",
                targetBiome.unwrapKey().map(k -> k.location().toString()).orElse("?"),
                round, walks, discoveredBlocks.blocks.size(), System.currentTimeMillis() - start);

        return discoveredBlocks;
    }

    /**
     * Returns {@code count} chunk-center block positions along an outward spiral around the origin, skipping the first
     * {@code start} points. Consecutive batches therefore cover distinct, ever-widening horizontal samples.
     */
    private static List<long[]> sampleColumns(int start) {
        int total = start + NodeUtils.COLUMNS_PER_ROUND;
        List<long[]> spiral = new ArrayList<>(total);
        int cx = 0, cz = 0, dx = 0, dz = -1;
        int step = 1, stepCount = 0, turnCount = 0;

        spiral.add(new long[]{8, 8});

        while (spiral.size() < total) {
            cx += dx;
            cz += dz;
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

            spiral.add(new long[]{cx * 16L + 8, cz * 16L + 8});
        }

        return spiral.subList(start, total);
    }
}