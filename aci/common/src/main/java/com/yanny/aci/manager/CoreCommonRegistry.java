package com.yanny.aci.manager;

import com.google.common.collect.HashBiMap;
import com.mojang.logging.LogUtils;
import com.yanny.aci.api.ICoreCommonRegistry;
import com.yanny.aci.api.ICoreCommonUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public abstract class CoreCommonRegistry<TConfig> extends BaseRegistry implements ICoreCommonRegistry, ICoreCommonUtils<TConfig> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final TConfig configuration;
    private final Set<String> translationKeys = new HashSet<>();

    private HashBiMap<String, Integer> dictionary = null;

    public CoreCommonRegistry() {
        configuration = loadConfiguration();
    }

    @NotNull
    protected abstract TConfig loadConfiguration();

    @NotNull
    @Override
    public final TConfig getConfiguration() {
        return configuration;
    }

    @Override
    public void registerTranslationKey(String key) {
        if (dictionary == null) {
            translationKeys.add(key);
        } else {
            LOGGER.warn("Trying to register key {} after registry freeze!", key);
        }
    }

    @NotNull
    public HashBiMap<String, Integer> getDictionary() {
        if (dictionary == null) {
            List<String> sorted = new ArrayList<>(translationKeys);
            Collections.sort(sorted);

            dictionary = HashBiMap.create(sorted.size());
            translationKeys.clear();

            for (int i = 0; i < sorted.size(); i++) {
                dictionary.put(sorted.get(i), i);
            }
        }

        return dictionary;
    }
}
