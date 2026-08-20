package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.awi.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;

/**
 * Determines, per biome, which blocks the dimension's surface rules place and at what vertical position.
 * <p>
 * The compiled surface rule is evaluated against a <b>physically consistent, canonical column</b>: flat terrain with
 * the surface at an assumed height {@code H}, solid below, water up to sea level, air above. All rule inputs
 * ({@code stoneDepthAbove/Below}, {@code waterHeight}, {@code minSurfaceLevel}) are derived from that column exactly
 * like {@code SurfaceSystem#buildSurface} does.
 * <p>
 * The assumed surface height {@code H} is swept across the world's build range and a handful of horizontal sample
 * points feed the real 2D surface/noise-threshold fields. Every rule hit is recorded both as an absolute Y and as a
 * depth below the assumed surface; a block is then classified empirically:
 * <ul>
 *     <li><b>surface-relative</b> (grass, dirt, sand, badlands bands) — its depth below the surface is stable while
 *     its absolute Y tracks {@code H}; reported as "depth below surface".</li>
 *     <li><b>absolute</b> (deepslate, bedrock) — its absolute Y is stable while its depth tracks {@code H}; reported
 *     as an absolute Y range.</li>
 * </ul>
 */
public class NodeUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    // How far below the preliminary surface vanilla builds surface (SurfaceRules.Context constant), used to derive minSurfaceLevel.
    private static final int SURFACE_BUILD_DEPTH = 8;

    /**
     * Sampling knobs, parameterised so the scan's cost can be swept against its coverage — see
     * {@code BaseLayoutSweepTest}. {@link #DEFAULT} is what production uses.
     * <p>
     * The scan converges: each round samples a fresh batch of horizontal points and a shifted surface-height phase; it
     * repeats while rounds keep discovering new (block, position) observations and stops once a full phase cycle adds
     * nothing.
     *
     * @param columnsPerRound     horizontal sample columns added per round
     * @param surfaceHeightStep   vertical stride between assumed surface heights within one round; consecutive rounds
     *                            shift the phase so a full cycle of {@code surfaceHeightStep} rounds retries every height
     * @param stableRounds        stop after this many consecutive rounds add nothing new
     * @param maxRounds           hard safety cap so a pathological rule cannot loop forever; hitting it is logged, never
     *                            silently truncated
     * @param maxCeilingThickness besides the solid column, each surface height is also probed as a thin floating stone
     *                            slab (air below it) so ceiling-gated rules fire: ON_CEILING ({@code stoneDepthBelow<=1},
     *                            e.g. badlands red_sandstone) and UNDER_CEILING ({@code stoneDepthBelow<=1+surfaceDepth}).
     *                            Slab thickness is swept {@code 1..this} to cover that window.
     * @param deepWalkWindow      how many blocks below the assumed surface a normal column walk still evaluates
     *                            ({@code 0} = down to the world bottom). Every Y is reached regardless, because the
     *                            assumed surface height sweeps the whole build range; only the combination "this Y with
     *                            a very large stone depth above it" stops being sampled
     * @param extentStableRounds  second, weaker stop condition ({@code 0} disables it): stop after this many consecutive
     *                            rounds in which nothing <i>widened</i> the reported result (no new block, flag, or wider
     *                            depth/Y extent), even though new positions keep turning up. It must stay the weaker of
     *                            the two conditions rather than replace {@code stableRounds}
     * @param specializeRulePerBiome recompile the surface rule per biome with the branches that cannot fire for it
     *                            removed — see {@link SurfaceRuleSpecializer}. Semantics-preserving, so it must never
     *                            change the result
     */
    public record ScanSettings(int columnsPerRound, int surfaceHeightStep, int stableRounds, int extentStableRounds,
                               int maxRounds, int maxCeilingThickness, int deepWalkWindow, boolean specializeRulePerBiome) {
        public static final ScanSettings DEFAULT = new ScanSettings(8, 4, 8, 12, 40, 8, 32, true);
    }

    /** The sampling knobs plus the run-time switches that are not part of them — currently only diagnostic logging. */
    public record ScanOptions(ScanSettings settings, boolean logStatistics) {
        public static final ScanOptions DEFAULT = new ScanOptions(ScanSettings.DEFAULT, false);
    }

    public static class DimensionContext {
        private final HolderLookup.Provider codecLookup;
        private final SurfaceRules.RuleSource masterSurfaceRule;
        private final SurfaceRules.Context context;
        private SurfaceRules.SurfaceRule compiledRule;
        /** Built on first use and reused for every biome of this dimension — the encode behind it is not free. */
        @Nullable
        private SurfaceRuleSpecializer specializer;
        private final int minBuildHeight;
        private final int maxBuildHeight;
        private final int seaLevel;
        private final BlockState defaultBlock;
        private final BlockState defaultFluid;
        private final BiomeHolderWrapper biomeWrapper = new BiomeHolderWrapper();

        /** @param codecLookup see {@link SurfaceRuleSpecializer}; {@code registryAccess} in game, a test's own provider otherwise. */
        public DimensionContext(RegistryAccess registryAccess, HolderLookup.Provider codecLookup, NoiseBasedChunkGenerator noiseGenerator,
                                RandomState randomState) {
            Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
            NoiseGeneratorSettings settings = noiseGenerator.generatorSettings().value();

            this.codecLookup = codecLookup;
            this.masterSurfaceRule = settings.surfaceRule();

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
            this.compiledRule = this.masterSurfaceRule.apply(this.context);
        }

        /** Points the context at the biome about to be scanned, compiling a rule specialized for it when asked to. */
        void useBiome(Holder<Biome> biome, ScanOptions options) {
            biomeWrapper.currentBiome = biome;

            if (options.settings().specializeRulePerBiome()) {
                if (specializer == null) {
                    specializer = new SurfaceRuleSpecializer(masterSurfaceRule, codecLookup, options.logStatistics());
                }

                compiledRule = specializer.specialize(biome).apply(context);
            }
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
        // Tracked on insert: the convergence check reads them once per round per block.
        private int min = Integer.MAX_VALUE;
        private int max = Integer.MIN_VALUE;

        public boolean add(int position) {
            if (positions.add(position)) {
                min = Math.min(min, position);
                max = Math.max(max, position);
                return true;
            }

            return false;
        }

        public int size() {
            return positions.size();
        }

        /** Whether two holders recorded exactly the same set of positions (used to detect floor==ceiling depths). */
        public boolean sameValuesAs(RangeHolder other) {
            return positions.equals(other.positions);
        }

        /** Number of disjoint contiguous ranges the recorded positions collapse into (1 == a single solid band). */
        public int clusterCount() {
            return buildRanges().size();
        }

        /** Lowest recorded position, or {@link Integer#MAX_VALUE} when nothing was recorded. */
        public int min() {
            return min;
        }

        /** Highest recorded position, or {@link Integer#MIN_VALUE} when nothing was recorded. */
        public int max() {
            return max;
        }

        /** Span between the lowest and highest recorded position (0 when empty or single-valued). */
        public int spread() {
            return positions.isEmpty() ? 0 : max - min;
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
        // badlands banded-terracotta strata, clay bands mod 192).
        private static final int LAYERED_MIN_BANDS = 3;

        final RangeHolder depths = new RangeHolder();
        final RangeHolder absolute = new RangeHolder();
        // Depths split by placement context: floor = normal below-surface placement (sandstone under the sand),
        // ceiling = ON_CEILING overhang placement (red_sandstone).
        final RangeHolder floorDepths = new RangeHolder();
        final RangeHolder ceilingDepths = new RangeHolder();
        // Which flooding contexts the rule placed this block in, taken from whether the walk had water above the surface.
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

        /**
         * Everything about this observation that can still widen the reported result: the extremes of each axis and the
         * water/placement flags. Interior positions are deliberately excluded — see {@link ScanSettings#convergeOnExtent}.
         */
        long extentSignature() {
            long signature = (seenUnderwater ? 1 : 0) + (seenDry ? 2 : 0);

            for (RangeHolder holder : List.of(depths, absolute, floorDepths, ceilingDepths)) {
                signature = signature * 31 + holder.min();
                signature = signature * 31 + holder.max();
            }

            return signature;
        }

        Kind classify(ScanSettings settings) {
            // Surface-relative: depth-below-surface stays tighter than absolute Y as the assumed surface height is
            // swept. A bounded deep walk censors the depth axis at the window, so a block whose observed depths run
            // into that window is treated as unbounded-depth (absolute) rather than as a very thick surface layer.
            boolean depthCensored = settings.deepWalkWindow() > 0 && depths.max() >= settings.deepWalkWindow() - 1;
            boolean surfaceRelative = !depthCensored && depths.spread() < absolute.spread();

            if (!surfaceRelative) {
                return Kind.ABSOLUTE;
            }
            // Layered strata recur at many separated absolute-Y bands AND span a depth window at least as thick as
            // the surface-height sampling step. Below that thickness the absolute-Y fragmentation is a sampling
            // artifact: the block is recorded once per swept surface height, so its Ys fall on a regular grid.
            if (depths.spread() >= settings.surfaceHeightStep() && absolute.clusterCount() >= LAYERED_MIN_BANDS) {
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
        /** The settings the observations were collected with — {@link BlockObservation#classify} is relative to them. */
        private final ScanSettings settings;
        /** How the scan ended, for diagnostics: rounds used, and whether it stopped only because of the safety cap. */
        private int rounds;
        private boolean hitRoundCap;

        LayerHolder(ScanSettings settings) {
            this.settings = settings;
        }

        public boolean isEmpty() {
            return blocks.isEmpty();
        }

        public int rounds() {
            return rounds;
        }

        public boolean hitRoundCap() {
            return hitRoundCap;
        }

        void record(Block block, int assumedSurface, int y, boolean underwater, boolean ceiling) {
            blocks.computeIfAbsent(block, k -> new BlockObservation()).record(assumedSurface - y, y, underwater, ceiling);
        }

        /** Total number of distinct (block, depth) and (block, absoluteY) observations collected so far. */
        long observationCount() {
            long total = 0;

            for (BlockObservation obs : blocks.values()) {
                total += obs.depths.size() + obs.absolute.size();
            }

            return total;
        }

        /** Only the extent of what would be reported: block set, flags, axis extremes — interior positions ignored. */
        long extentSignature() {
            long total = 0;

            for (Map.Entry<Block, BlockObservation> entry : blocks.entrySet()) {
                total += entry.getKey().hashCode() * 31L + entry.getValue().extentSignature();
            }

            return total;
        }

        /**
         * Snapshots every discovered block as one or more {@link BlockInfo}s: its storage type (surface-relative depth
         * vs. absolute Y vs. layered strata), the value ranges, its water constraint, and its placement.
         * <p>
         * Surface-relative (depth) blocks are split by placement when their floor and ceiling depths genuinely differ,
         * so a block that is both a normal below-surface layer <i>and</i> an exposed overhang (e.g. warm-ocean
         * sandstone) keeps both modes. Absolute/layered blocks are keyed by absolute Y, which placement does not
         * change, so they stay a single entry.
         */
        public Set<BlockInfo> getBlockInfos() {
            Set<BlockInfo> infos = new HashSet<>();

            for (Map.Entry<Block, BlockObservation> entry : blocks.entrySet()) {
                Block block = entry.getKey();
                BlockObservation obs = entry.getValue();
                WaterConstraint water = obs.waterConstraint();

                switch (obs.classify(settings)) {
                    case SURFACE -> {
                        boolean hasFloor = obs.floorDepths.size() > 0;
                        boolean hasCeiling = obs.ceilingDepths.size() > 0;

                        if (hasFloor && hasCeiling && !obs.floorDepths.sameValuesAs(obs.ceilingDepths)) {
                            // Genuinely two placement modes with different depths — keep both.
                            infos.add(new BlockInfo(block, StorageType.RELATIVE, obs.floorDepths.buildRanges(), water, Placement.FLOOR));
                            infos.add(new BlockInfo(block, StorageType.RELATIVE, obs.ceilingDepths.buildRanges(), water, Placement.CEILING));
                        } else if (hasCeiling && !hasFloor) {
                            // Overhang-only block (badlands red_sandstone).
                            infos.add(new BlockInfo(block, StorageType.RELATIVE, obs.ceilingDepths.buildRanges(), water, Placement.CEILING));
                        } else {
                            // Floor-only, or floor and ceiling identical: one entry (ANY == no overhang annotation).
                            infos.add(new BlockInfo(block, StorageType.RELATIVE, obs.floorDepths.buildRanges(), water, obs.placement()));
                        }
                    }
                    case ABSOLUTE -> infos.add(new BlockInfo(block, StorageType.ABSOLUTE, obs.absolute.buildRanges(), water, obs.placement()));
                    case LAYERED -> infos.add(new BlockInfo(block, StorageType.LAYERED, obs.absolute.buildRanges(), water, obs.placement()));
                    default -> throw new IllegalStateException("Unknown kind for block " + block);
                }
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
     * {@code walkBottom} is where the walk stops evaluating, which is not necessarily where the modelled stone run ends:
     * the run's shape (and with it {@code stoneDepthBelow}) still comes from {@code stoneBottom}. The caller must have
     * called {@link SurfaceRules.Context#updateXZ} for {@code posX}/{@code posZ}.
     */
    private static void walkColumn(DimensionContext dimCtx, LayerHolder holder, int posX, int posZ,
                                   int surfaceTop, int stoneBottom, int walkBottom, boolean hasWaterAbove) {
        SurfaceRules.Context context = dimCtx.context;
        SurfaceRules.SurfaceRule rule = dimCtx.compiledRule;
        int seaLevel = dimCtx.seaLevel;

        // Pin the preliminary surface to the assumed surface height so that surface-relative conditions
        // (above_preliminary_surface) resolve against it, exactly as vanilla derives minSurfaceLevel.
        context.minSurfaceLevel = surfaceTop + context.surfaceDepth - SURFACE_BUILD_DEPTH;
        context.lastMinSurfaceLevelUpdate = context.lastUpdateXZ;

        boolean water = hasWaterAbove && surfaceTop < seaLevel;
        int top = water ? seaLevel : surfaceTop;

        int stoneDepthAbove = 0;
        int waterHeight = Integer.MIN_VALUE;

        for (int y = top; y >= walkBottom; y--) {
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
    public static LayerHolder getBaseBlocksForBiome(DimensionContext dimCtx, Holder<Biome> targetBiome, ScanOptions options) {
        ScanSettings settings = options.settings();
        LayerHolder discoveredBlocks = new LayerHolder(settings);
        int round = 0;
        int stableRounds = 0;
        int extentStableRounds = 0;
        long observations = 0;
        long extent = 0;

        try {
            dimCtx.useBiome(targetBiome, options);

            // Repeat until a full phase cycle discovers nothing new — or, for rules whose interior never settles, until
            // nothing has widened the result for a (longer) while. Plus the safety cap.
            while (stableRounds < settings.stableRounds() && round < settings.maxRounds()
                    && (settings.extentStableRounds() <= 0 || extentStableRounds < settings.extentStableRounds())) {
                long beforeObservations = observations;
                long beforeExtent = extent;
                // Fresh horizontal points every round (new 2D noise values) and a shifted surface-height phase so that,
                // over a full cycle, every possible surface height is retried (fills absolute-Y gaps in volumetric dims).
                int heightPhase = round % settings.surfaceHeightStep();
                List<long[]> samples = sampleColumns(round * settings.columnsPerRound(), settings.columnsPerRound());

                for (long[] xz : samples) {
                    int posX = (int) xz[0];
                    int posZ = (int) xz[1];

                    // Once per column: every walk below shares these 2D noise values.
                    dimCtx.context.updateXZ(posX, posZ);

                    for (int h = dimCtx.maxBuildHeight - 1 - heightPhase; h >= dimCtx.minBuildHeight; h -= settings.surfaceHeightStep()) {
                        // Normal surface: solid stone from the world bottom up to h. The walk itself may stop early
                        // (deepWalkWindow) — every Y is still reached, because h itself sweeps the whole build range.
                        int walkBottom = settings.deepWalkWindow() > 0
                                ? Math.max(dimCtx.minBuildHeight, h - settings.deepWalkWindow() + 1)
                                : dimCtx.minBuildHeight;

                        walkColumn(dimCtx, discoveredBlocks, posX, posZ, h, dimCtx.minBuildHeight, walkBottom, true);

                        // Overhang surfaces: thin floating slabs so ceiling-gated rules fire. Dry slabs cover land
                        // overhangs (badlands red_sandstone); below sea level a water-covered slab is also probed so
                        // underwater ceiling rules fire (lukewarm-ocean sandstone), which a dry column never reaches.
                        for (int thickness = 1; thickness <= settings.maxCeilingThickness(); thickness++) {
                            int stoneBottom = h - thickness + 1;

                            if (stoneBottom < dimCtx.minBuildHeight) {
                                break;
                            }

                            walkColumn(dimCtx, discoveredBlocks, posX, posZ, h, stoneBottom, stoneBottom, false);

                            if (h < dimCtx.seaLevel) {
                                walkColumn(dimCtx, discoveredBlocks, posX, posZ, h, stoneBottom, stoneBottom, true);
                            }
                        }
                    }
                }

                observations = discoveredBlocks.observationCount();
                extent = discoveredBlocks.extentSignature();
                stableRounds = (observations == beforeObservations) ? stableRounds + 1 : 0;
                extentStableRounds = (extent == beforeExtent) ? extentStableRounds + 1 : 0;
                round++;
            }
        } catch (Throwable t) {
            LOGGER.warn("Surface scan failed for biome {}", targetBiome.unwrapKey().map(Object::toString).orElse("?"), t);
        }

        discoveredBlocks.rounds = round;
        discoveredBlocks.hitRoundCap = round >= settings.maxRounds();

        if (round >= settings.maxRounds()) {
            LOGGER.warn("Surface scan for biome {} hit the {}-round cap; coverage may be incomplete",
                    targetBiome.unwrapKey().map(Object::toString).orElse("?"), settings.maxRounds());
        }

        return discoveredBlocks;
    }

    /**
     * Returns {@code count} chunk-center block positions along an outward spiral around the origin, skipping the first
     * {@code start} points. Consecutive batches therefore cover distinct, ever-widening horizontal samples.
     */
    private static List<long[]> sampleColumns(int start, int count) {
        int total = start + count;
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