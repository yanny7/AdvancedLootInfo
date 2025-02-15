package com.yanny.ali.manager;

import com.yanny.ali.api.IPlugin;

public record PluginHolder(String modId, IPlugin plugin) {}
