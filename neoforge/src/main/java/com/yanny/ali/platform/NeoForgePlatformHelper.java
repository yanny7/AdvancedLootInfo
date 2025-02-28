package com.yanny.ali.platform;

import com.yanny.ali.manager.PluginHolder;
import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DistHolder;
import com.yanny.ali.platform.services.IPlatformHelper;

import java.util.List;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public List<PluginHolder> getPlugins() {
        return List.of();
    }

    @Override
    public DistHolder<AbstractClient, AbstractServer> getInfoPropagator() {
        return null;
    }
}