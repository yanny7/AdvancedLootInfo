package com.yanny.aci.tooltip;

import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.ExecutionException;

public class CoreTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static <K, V> V getFromCache(LoadingCache<K, V> cache, K key) {
        try {
            return cache.get(key);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to retrieve node from cache", e);
        }
    }

    public static void logCacheStatistics(LoadingCache<?, ?> cache, Identifier id) {
        CacheStats stats = cache.stats();

        LOGGER.info("Statistics for Cache: {}", id);
        LOGGER.info("Total Requests: {}", stats.requestCount());
        LOGGER.info("Hits (Reused):  {} ({})", stats.hitCount(), String.format("%.2f%%", stats.hitRate() * 100));
        LOGGER.info("Misses (New):   {} ({})", stats.missCount(), String.format("%.2f%%", stats.missRate() * 100));
        LOGGER.info("Load Penalty:   {}ms (avg)", stats.averageLoadPenalty() / 1_000_000.0);
        LOGGER.info("Evictions:      {}", stats.evictionCount());
    }
}
