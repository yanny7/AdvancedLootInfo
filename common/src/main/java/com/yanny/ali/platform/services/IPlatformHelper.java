package com.yanny.ali.platform.services;

import com.yanny.ali.manager.PluginHolder;
import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DistHolder;

import java.util.List;

public interface IPlatformHelper {
        List<PluginHolder> getPlugins();

        DistHolder<AbstractClient, AbstractServer> getInfoPropagator();
}