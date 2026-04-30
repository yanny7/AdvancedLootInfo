package com.yanny.aci.manager;

import com.yanny.aci.api.ICoreCommonUtils;
import org.jetbrains.annotations.NotNull;

public abstract class CoreCommonRegistry<TConfig> extends BaseRegistry implements ICoreCommonUtils<TConfig> {
    private final TConfig configuration;

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
}
