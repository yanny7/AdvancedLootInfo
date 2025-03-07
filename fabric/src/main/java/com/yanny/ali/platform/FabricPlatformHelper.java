package com.yanny.ali.platform;

import com.mojang.logging.LogUtils;
import com.yanny.ali.CommonAliMod;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.manager.PluginHolder;
import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.DistHolder;
import com.yanny.ali.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class FabricPlatformHelper implements IPlatformHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public List<PluginHolder> getPlugins() {
        List<PluginHolder> plugins = new LinkedList<>();

        for (EntrypointContainer<IPlugin> container : FabricLoader.getInstance().getEntrypointContainers("ali", IPlugin.class)) {
            try {
                plugins.add(new PluginHolder(container.getProvider().getMetadata().getId(), container.getEntrypoint()));
            } catch (Throwable t) {
                LOGGER.warn("Failed to load plugin with error: {}", t.getMessage());
            }
        }

        LOGGER.info("Found {} plugin(s", plugins.size());
        return plugins;
    }

    @Override
    public DistHolder<AbstractClient, AbstractServer> getInfoPropagator() {
        return CommonAliMod.INFO_PROPAGATOR;
    }
}
