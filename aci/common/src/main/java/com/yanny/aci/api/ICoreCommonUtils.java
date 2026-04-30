package com.yanny.aci.api;

import org.jetbrains.annotations.NotNull;

public interface ICoreCommonUtils<TConfig> {
    @NotNull
    TConfig getConfiguration();
}
