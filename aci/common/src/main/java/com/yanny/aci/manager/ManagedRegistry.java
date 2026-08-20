package com.yanny.aci.manager;

import com.yanny.aci.CommonLogUtils;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class ManagedRegistry<K, V> {
    private final Logger logger;
    private final Map<K, V> storage;
    @Nullable
    private final Registry<?> registry;
    private final Function<K, String> keyNameGetter;
    private final String label;
    @Nullable
    private final Set<K> missing;

    public ManagedRegistry(String modId, String label, boolean reportMissing, Supplier<Map<K, V>> mapSupplier, Function<K, String> keyNameGetter, @Nullable Registry<?> registry) {
        this.logger = CommonLogUtils.getLogger(modId);
        this.label = label;
        this.keyNameGetter = keyNameGetter;
        this.registry = registry;
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
            missing.forEach((t) -> logger.warn("Missing {} for {}", label, keyNameGetter.apply(t)));
        }
    }

    public void logStatistics() {
        if (registry != null) {
            logger.info("Registered {}/{} {}", storage.size(), registry.size(), label);
        } else {
            logger.info("Registered {} {}", storage.size(), label);
        }
    }
}
