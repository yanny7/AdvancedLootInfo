package com.yanny.aci.platform;

import com.yanny.aci.api.ICorePlugin;

import java.nio.file.Path;
import java.util.List;

public interface ICorePlatformHelper<T extends ICorePlugin<?, ?, ?>> {
        List<T> getPlugins();

        Path getConfiguration();
}