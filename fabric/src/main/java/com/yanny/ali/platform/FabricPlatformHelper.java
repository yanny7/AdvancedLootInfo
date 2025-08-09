package com.yanny.ali.platform;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.manager.PluginHolder;
import com.yanny.ali.pip.BlockRenderState;
import com.yanny.ali.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.gui.GuiGraphics;
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
                IPlugin plugin = container.getEntrypoint();

                plugins.add(new PluginHolder(container.getProvider().getMetadata().getId(), plugin));
                LOGGER.info("Registered plugin {}", plugin.getClass().getCanonicalName());
            } catch (Throwable t) {
                LOGGER.warn("Failed to load plugin with error: {}", t.getMessage());
            }
        }

        LOGGER.info("Found {} plugin(s)", plugins.size());
        return plugins;
    }

    @Override
    public void renderBlockInGui(GuiGraphics guiGraphics, BlockRenderState renderState) {
//        guiGraphics.submitPictureInPictureRenderState(renderState); FIXME how?
    }
}
