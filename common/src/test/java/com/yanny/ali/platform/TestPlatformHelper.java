package com.yanny.ali.platform;

import com.yanny.ali.manager.PluginHolder;
import com.yanny.ali.platform.services.IPlatformHelper;
import com.yanny.ali.plugin.Plugin;

import java.util.List;

public class TestPlatformHelper implements IPlatformHelper {
    @Override
    public List<PluginHolder> getPlugins() {
        return List.of(new PluginHolder("test", new Plugin()));
    }
}
