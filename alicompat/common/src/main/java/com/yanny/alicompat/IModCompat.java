package com.yanny.alicompat;

import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.ICommonRegistry;
import com.yanny.ali.api.IServerRegistry;
import org.jetbrains.annotations.NotNull;

public interface IModCompat {
    @NotNull
    String targetModId();

    default void registerCommon(ICommonRegistry registry) {}

    default void registerClient(IClientRegistry registry) {}

    default void registerServer(IServerRegistry registry) {}
}
