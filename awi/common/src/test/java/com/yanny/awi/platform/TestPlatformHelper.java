package com.yanny.awi.platform;

import com.yanny.awi.api.IPlugin;
import com.yanny.awi.platform.services.IPlatformHelper;
import com.yanny.awi.plugin.Plugin;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class TestPlatformHelper implements IPlatformHelper {
    @NotNull
    @Override
    public List<IPlugin> getPlugins() {
        return List.of(new Plugin());
    }

    @NotNull
    @Override
    public Path getConfiguration() {
        return null;
    }

    @NotNull
    @Override
    public HolderLookup.Provider getLookupProvider() {
        return null;
    }
}
