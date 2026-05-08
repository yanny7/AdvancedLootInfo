package com.yanny.aci.api;

import org.jetbrains.annotations.NotNull;

public interface ICorePlugin<
        TCommonRegistry,
        TClientRegistry extends ICoreClientRegistry<?, ?, ?>,
        TServerRegistry
        > {
    @NotNull
    String getModId();

    default void registerCommon(TCommonRegistry registry) {}

    default void registerClient(TClientRegistry registry) {}

    default void registerServer(TServerRegistry registry) {}
}
