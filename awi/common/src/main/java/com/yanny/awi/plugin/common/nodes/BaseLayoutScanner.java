package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.CommonLogUtils;
import com.yanny.awi.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Runs {@link NodeUtils#getBaseBlocksForBiome} for every (dimension, biome) pair of a world up front, on one shared
 * thread pool.
 * <p>
 * Results are cached per (noise settings, biome): the scan's outcome depends only on the dimension's
 * {@link NoiseGeneratorSettings} (surface rule, height range, sea level, default block/fluid), the biome and the world
 * seed, so dimensions sharing settings scan each biome once.
 */
public class BaseLayoutScanner {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    private final Map<ResourceLocation, Map<Holder<Biome>, NodeUtils.LayerHolder>> resultsByDimension;
    private final Stats stats;

    /**
     * {@code totalTimeMs} is the real elapsed time of the whole parallel scan phase. The two surface-rule counts say how
     * much wider the result cache could get: {@code distinctSurfaceRules} counts rules that are structurally equal,
     * {@code sharedSurfaceRuleInstances} only those that are the very same object.
     */
    public record Stats(long totalTimeMs, int scannedBiomeCount, int cachedBiomeCount,
                        long minBiomeTimeMs, long maxBiomeTimeMs, double meanBiomeTimeMs,
                        int scannedDimensionCount, int distinctSurfaceRules, int distinctSurfaceRuleInstances,
                        List<DimensionCost> costliestDimensions) {}

    /** Summed scan time of one dimension. */
    public record DimensionCost(ResourceLocation dimension, long timeMs, int biomeCount, int roundCappedCount) {}

    private record CacheKey(Object settings, Object biome) {}

    private record Task(ResourceLocation dimension, NoiseBasedChunkGenerator generator, Holder<Biome> biome) {}

    private record TaskResult(Task task, NodeUtils.LayerHolder layers, long durationNanos, boolean cached) {}

    private BaseLayoutScanner(Map<ResourceLocation, Map<Holder<Biome>, NodeUtils.LayerHolder>> resultsByDimension, Stats stats) {
        this.resultsByDimension = resultsByDimension;
        this.stats = stats;
    }

    @NotNull
    public static BaseLayoutScanner scan(ServerLevel level, Registry<LevelStem> levelStemRegistry, boolean logStatistics) {
        return scan(level.registryAccess(), level.registryAccess(), level.getSeed(), levelStemRegistry,
                NodeUtils.ScanSettings.DEFAULT, logStatistics);
    }

    /** @param codecLookup see {@link SurfaceRuleSpecializer}; the same object as {@code registryAccess} in game. */
    @NotNull
    public static BaseLayoutScanner scan(RegistryAccess registryAccess, HolderLookup.Provider codecLookup, long seed,
                                         Registry<LevelStem> levelStemRegistry, NodeUtils.ScanSettings scanSettings,
                                         boolean logStatistics) {
        List<Task> tasks = new ArrayList<>();

        // Grouped by dimension so each worker keeps reusing the DimensionContext it already built (see ContextCache).
        for (LevelStem levelStem : levelStemRegistry) {
            if (levelStem.generator() instanceof NoiseBasedChunkGenerator generator) {
                ResourceLocation dimension = levelStemRegistry.getKey(levelStem);

                for (Holder<Biome> biome : generator.getBiomeSource().possibleBiomes()) {
                    tasks.add(new Task(dimension, generator, biome));
                }
            }
        }

        // Per dimension, not per task: a structural HashSet of rules hashes whole rule trees. The identity set filters
        // first, so equals/hashCode runs a handful of times instead of once per biome.
        Set<SurfaceRules.RuleSource> distinctRuleInstances = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<ResourceLocation> scannedDimensions = new HashSet<>();

        for (Task task : tasks) {
            if (scannedDimensions.add(task.dimension())) {
                distinctRuleInstances.add(task.generator().generatorSettings().value().surfaceRule());
            }
        }

        Set<SurfaceRules.RuleSource> distinctRules = new HashSet<>(distinctRuleInstances);

        Map<CacheKey, NodeUtils.LayerHolder> cache = new ConcurrentHashMap<>();
        ThreadLocal<ContextCache> threadLocalCtx = ThreadLocal.withInitial(ContextCache::new);
        NodeUtils.ScanOptions scanOptions = new NodeUtils.ScanOptions(scanSettings, logStatistics);
        int threadCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        Map<ResourceLocation, Map<Holder<Biome>, NodeUtils.LayerHolder>> results = new HashMap<>();
        List<Long> scanDurations = new ArrayList<>();
        Map<ResourceLocation, DimensionCost> costs = new HashMap<>();
        int cachedCount = 0;
        long startTime = System.nanoTime();

        try {
            List<Future<TaskResult>> futures = tasks.stream()
                    .map((task) -> executor.submit(() -> runTask(task, registryAccess, codecLookup, seed, cache, threadLocalCtx.get(), scanOptions)))
                    .toList();

            for (int i = 0; i < futures.size(); i++) {
                Task task = tasks.get(i);

                try {
                    TaskResult result = futures.get(i).get();

                    results.computeIfAbsent(task.dimension(), (k) -> new HashMap<>()).put(task.biome(), result.layers());

                    if (result.cached()) {
                        cachedCount++;
                    } else {
                        scanDurations.add(result.durationNanos());
                    }

                    DimensionCost previous = costs.get(task.dimension());
                    int capped = result.layers().hitRoundCap() ? 1 : 0;

                    costs.put(task.dimension(), previous == null
                            ? new DimensionCost(task.dimension(), result.durationNanos() / 1_000_000L, 1, capped)
                            : new DimensionCost(task.dimension(), previous.timeMs() + result.durationNanos() / 1_000_000L,
                                    previous.biomeCount() + 1, previous.roundCappedCount() + capped));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error("Base layout scan interrupted for biome {} in {}", biomeName(task.biome()), task.dimension(), e);
                } catch (Exception e) {
                    LOGGER.error("Failed to scan base layout for biome {} in {}", biomeName(task.biome()), task.dimension(), e);
                }
            }
        } finally {
            executor.shutdown();
        }

        List<DimensionCost> costliest = costs.values().stream()
                .sorted(Comparator.comparingLong(DimensionCost::timeMs).reversed())
                .limit(5)
                .toList();

        return new BaseLayoutScanner(results, buildStats(System.nanoTime() - startTime, scanDurations, cachedCount,
                scannedDimensions.size(), distinctRules.size(), distinctRuleInstances.size(), costliest));
    }

    @NotNull
    private static TaskResult runTask(Task task, RegistryAccess registryAccess, HolderLookup.Provider codecLookup, long seed,
                                      Map<CacheKey, NodeUtils.LayerHolder> cache, ContextCache contextCache,
                                      NodeUtils.ScanOptions scanOptions) {
        CacheKey key = new CacheKey(settingsKey(task.generator()), biomeKey(task.biome()));
        NodeUtils.LayerHolder cached = cache.get(key);

        if (cached != null) {
            return new TaskResult(task, cached, 0, true);
        }

        NodeUtils.DimensionContext ctx = contextCache.get(task, registryAccess, codecLookup, seed);
        long startTime = System.nanoTime();
        NodeUtils.LayerHolder layers = NodeUtils.getBaseBlocksForBiome(ctx, task.biome(), scanOptions);
        long duration = System.nanoTime() - startTime;

        cache.putIfAbsent(key, layers);

        return new TaskResult(task, layers, duration, false);
    }

    @NotNull
    private static Stats buildStats(long elapsedNanos, List<Long> scanDurations, int cachedCount, int dimensionCount,
                                    int distinctRules, int distinctRuleInstances, List<DimensionCost> costliest) {
        long min = 0;
        long max = 0;
        double mean = 0;

        if (!scanDurations.isEmpty()) {
            long sum = 0;

            min = Long.MAX_VALUE;

            for (long nanos : scanDurations) {
                min = Math.min(min, nanos);
                max = Math.max(max, nanos);
                sum += nanos;
            }

            min /= 1_000_000L;
            max /= 1_000_000L;
            mean = (double) sum / scanDurations.size() / 1_000_000.0;
        }

        return new Stats(elapsedNanos / 1_000_000L, scanDurations.size(), cachedCount, min, max, mean,
                dimensionCount, distinctRules, distinctRuleInstances, costliest);
    }

    /** Base blocks discovered for one dimension, keyed by biome; empty for dimensions without a noise generator. */
    @NotNull
    public Map<Holder<Biome>, NodeUtils.LayerHolder> getBaseLayouts(@Nullable ResourceLocation dimension) {
        return resultsByDimension.getOrDefault(dimension, Map.of());
    }

    @NotNull
    public Stats getStats() {
        return stats;
    }

    /**
     * Per-thread {@link NodeUtils.DimensionContext} holder. Building one creates a {@link RandomState}, so it is kept
     * for as long as the worker keeps receiving tasks of the same dimension; only one context per thread is alive at a
     * time, which bounds the memory the mock chunks hold.
     */
    private static class ContextCache {
        private ResourceLocation dimension;
        private NodeUtils.DimensionContext context;

        @NotNull
        NodeUtils.DimensionContext get(Task task, RegistryAccess registryAccess, HolderLookup.Provider codecLookup, long seed) {
            if (context == null || !task.dimension().equals(dimension)) {
                RandomState randomState = RandomState.create(
                        task.generator().generatorSettings().value(),
                        registryAccess.lookupOrThrow(Registries.NOISE),
                        seed
                );

                dimension = task.dimension();
                context = new NodeUtils.DimensionContext(registryAccess, codecLookup, task.generator(), randomState);
            }

            return context;
        }
    }

    /** Dimensions sharing noise settings produce identical scans, so the settings' registry key is the cache key. */
    @NotNull
    private static Object settingsKey(NoiseBasedChunkGenerator generator) {
        Holder<NoiseGeneratorSettings> settings = generator.generatorSettings();
        return settings.unwrapKey().map((key) -> (Object) key.location()).orElseGet(settings::value);
    }

    @NotNull
    private static Object biomeKey(Holder<Biome> biome) {
        return biome.unwrapKey().map((key) -> (Object) key.location()).orElseGet(biome::value);
    }

    @NotNull
    private static String biomeName(Holder<Biome> biome) {
        return biome.unwrapKey().map((key) -> key.location().toString()).orElse("<unnamed biome>");
    }
}
