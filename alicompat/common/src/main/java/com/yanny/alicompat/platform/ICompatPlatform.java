package com.yanny.alicompat.platform;

import org.jetbrains.annotations.NotNull;

public interface ICompatPlatform {
    boolean isModLoaded(@NotNull String modId);
}
