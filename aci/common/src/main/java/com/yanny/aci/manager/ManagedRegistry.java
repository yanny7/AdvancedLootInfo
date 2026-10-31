package com.yanny.aci.manager;

import com.yanny.aci.CommonLogUtils;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ManagedRegistry<K, V> {
    private static final String LAMBDA_MARKER = "$$Lambda";

    private final Logger logger;
    private final Map<K, V> storage;
    @Nullable
    private final Registry<?> registry;
    private final Function<K, String> keyNameGetter;
    private final String label;
    @Nullable
    private final Set<String> missing;
    @Nullable
    private final Set<String> autoDetected;

    public ManagedRegistry(String modId, String label, boolean reportMissing, Supplier<Map<K, V>> mapSupplier, Function<K, String> keyNameGetter, @Nullable Registry<?> registry) {
        this.logger = CommonLogUtils.getLogger(modId);
        this.label = label;
        this.keyNameGetter = keyNameGetter;
        this.registry = registry;
        storage = mapSupplier.get();

        if (reportMissing) {
            missing = new HashSet<>();
            autoDetected = new HashSet<>();
        } else {
            missing = null;
            autoDetected = null;
        }
    }

    public void clear() {
        storage.clear();

        if (missing != null) {
            missing.clear();
        }

        if (autoDetected != null) {
            autoDetected.clear();
        }
    }

    public void put(K key, V value) {
        storage.put(key, value);
    }

    public Map<K, V> entries() {
        return Collections.unmodifiableMap(storage);
    }

    public Optional<V> get(K key) {
        Optional<V> value = find(key);

        if (value.isEmpty()) {
            reportMissing(key);
        }

        return value;
    }

    public Optional<V> find(K key) {
        return Optional.ofNullable(storage.get(key));
    }

    public void reportMissing(K key) {
        if (missing != null) {
            missing.add(keyNameGetter.apply(key));
        }
    }

    public void reportAutoDetected(K key) {
        if (autoDetected != null) {
            autoDetected.add(keyNameGetter.apply(key));
        }
    }

    public void logMissing(boolean logAutoDetected) {
        if (missing != null) {
            missing.forEach((t) -> logger.warn("Missing {} for {}", label, t));
        }

        if (logAutoDetected && autoDetected != null) {
            autoDetected.stream().filter((t) -> missing == null || !missing.contains(t)).forEach((t) -> logger.info("No {} for {}, auto-detected", label, t));
        }
    }

    public void logStatistics() {
        if (registry != null) {
            logger.info("Registered {}/{} {}", storage.size(), registry.size(), label);
        } else {
            logger.info("Registered {} {}", storage.size(), label);
        }
    }

    @NotNull
    public static String classKeyName(Class<?> clazz) {
        String name = clazz.getTypeName();
        int index = name.indexOf(LAMBDA_MARKER);

        if (index >= 0) {
            return name.substring(0, index + LAMBDA_MARKER.length());
        }

        return name;
    }

    @NotNull
    public static String shortClassName(Class<?> clazz) {
        String name = clazz.getTypeName();
        int index = name.indexOf(LAMBDA_MARKER);

        if (index >= 0) {
            name = name.substring(0, index);
        } else if (!clazz.getSimpleName().isEmpty()) {
            return clazz.getSimpleName();
        }

        return name.substring(name.lastIndexOf('.') + 1);
    }
}
