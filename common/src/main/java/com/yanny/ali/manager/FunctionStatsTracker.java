package com.yanny.ali.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionStatsTracker {
    private final Map<String, Long> callCounts = new HashMap<>();
    private final Logger LOGGER;

    public FunctionStatsTracker(Class<?> clazz) {
        LOGGER = LoggerFactory.getLogger(clazz);
    }

    public void incrementCallCount(Class<?> function) {
        if (!PluginManager.COMMON_REGISTRY.getConfiguration().logMoreStatistics) {
            return;
        }

        String functionName = function.getTypeName();

        callCounts.put(functionName, callCounts.getOrDefault(functionName, 0L) + 1);
    }

    public void logStats() {
        if (!LOGGER.isInfoEnabled() || !PluginManager.COMMON_REGISTRY.getConfiguration().logMoreStatistics) {
            return;
        }

        Map<String, Long> top10 = callCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        LOGGER.info("--- Top 10 Most Called Functions ---");
        top10.forEach((functionName, count) -> LOGGER.info("{}: {} calls", functionName, count));
    }

    public void clearStats() {
        callCounts.clear();
    }
}