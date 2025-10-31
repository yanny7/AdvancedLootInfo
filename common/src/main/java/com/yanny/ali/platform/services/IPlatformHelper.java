package com.yanny.ali.platform.services;

import com.yanny.ali.api.IPlugin;

import java.nio.file.Path;
import java.util.List;

public interface IPlatformHelper {
        List<IPlugin> getPlugins();

        Path getConfiguration();
}