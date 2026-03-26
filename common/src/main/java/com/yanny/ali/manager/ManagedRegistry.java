package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class ManagedRegistry<K, V> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<K, V> storage;
    private final Function<K, String> keyNameGetter;
    private final String label;
    @Nullable
    private final Set<K> missing;

    public ManagedRegistry(String label, boolean reportMissing, Supplier<Map<K, V>> mapSupplier, Function<K, String> keyNameGetter) {
        this.label = label;
        this.keyNameGetter = keyNameGetter;
        storage = mapSupplier.get();

        if (reportMissing) {
            missing = new HashSet<>();
        } else {
            missing = null;
        }
    }

    public void clear() {
        storage.clear();

        if (missing != null) {
            missing.clear();
        }
    }

    public void put(K key, V value) {
        storage.put(key, value);
    }

    public Optional<V> get(K key) {
        V value = storage.get(key);

        if (value != null) {
            return Optional.of(value);
        }

        if (missing != null) {
            missing.add(key);
        }
        
        return Optional.empty();
    }

    public void logMissing() {
        if (missing != null) {
            missing.forEach((t) -> LOGGER.warn("Missing {} for {}", label, keyNameGetter.apply(t)));
        }
    }

    public void logStatistics() {
        LOGGER.info("Registered {} {}", storage.size(), label);
    }
}
