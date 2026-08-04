package com.yanny.aci.platform;

import com.yanny.aci.api.ICorePlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public interface ICorePlatformHelper<T extends ICorePlugin<?, ?, ?>> {
        @NotNull
        List<T> getPlugins();

        @NotNull
        Path getConfiguration();
}