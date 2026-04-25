package com.yanny.aci.manager;

import com.yanny.aci.api.ICoreCommonUtils;
import org.jetbrains.annotations.NotNull;

public abstract class CoreCommonRegistry<CN> extends BaseRegistry implements ICoreCommonUtils<CN> {
    private final CN configuration;

    public CoreCommonRegistry() {
        configuration = loadConfiguration();
    }

    @NotNull
    protected abstract CN loadConfiguration();

    @NotNull
    @Override
    public final CN getConfiguration() {
        return configuration;
    }
}
