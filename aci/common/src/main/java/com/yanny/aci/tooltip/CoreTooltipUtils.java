package com.yanny.aci.tooltip;

import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.ICoreTooltipNode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
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

    @NotNull
    public static <
            TServerUtils extends ICoreServerUtils<?, ?, ?>,
            TTooltipNode extends ICoreTooltipNode<TServerUtils>
            > List<Component> toComponents(List<TTooltipNode> tooltip, int pad, boolean showAdvancedTooltip) {
        List<Component> components = new ArrayList<>();

        for (TTooltipNode node : tooltip) {
            components.addAll(toComponents(node, pad, showAdvancedTooltip));
        }

        return components;
    }

    @NotNull
    public static <
            TServerUtils extends ICoreServerUtils<?, ?, ?>,
            TTooltipNode extends ICoreTooltipNode<TServerUtils>
            > List<Component> toComponents(TTooltipNode tooltip, int pad, boolean showAdvancedTooltip) {
        return tooltip.getComponents(pad, showAdvancedTooltip);
    }
}
