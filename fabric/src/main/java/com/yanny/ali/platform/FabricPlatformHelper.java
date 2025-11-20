package com.yanny.ali.platform;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.pip.BlockRenderState;
import com.yanny.ali.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class FabricPlatformHelper implements IPlatformHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public List<IPlugin> getPlugins() {
        List<IPlugin> plugins = new LinkedList<>();

        for (EntrypointContainer<IPlugin> container : FabricLoader.getInstance().getEntrypointContainers("ali", IPlugin.class)) {
            try {
                IPlugin plugin = container.getEntrypoint();

                if (FabricLoader.getInstance().isModLoaded(plugin.getModId())) {
                    plugins.add(plugin);
                    LOGGER.info("Registered ALI plugin [{}] {}", plugin.getModId(), plugin.getClass().getCanonicalName());
                }
            } catch (Throwable t) {
                LOGGER.warn("Failed to load plugin with error: {}", t.getMessage());
                t.printStackTrace();
            }
        }

        LOGGER.info("Found {} plugin(s)", plugins.size());
        return plugins;
    }

    @Override
    public Path getConfiguration() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public SpawnEggItem getSpawnEggItem(EntityType<?> entityType) {
        return SpawnEggItem.byId(entityType);
    }

    @Override
    public void renderBlockInGui(GuiGraphics guiGraphics, BlockRenderState renderState) {
//        guiGraphics.submitPictureInPictureRenderState(renderState); FIXME how?
    }
}
