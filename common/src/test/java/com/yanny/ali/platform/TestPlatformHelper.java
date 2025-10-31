package com.yanny.ali.platform;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.pip.BlockRenderState;
import com.yanny.ali.platform.services.IPlatformHelper;
import com.yanny.ali.plugin.Plugin;
import net.minecraft.client.gui.GuiGraphics;

import java.nio.file.Path;
import java.util.List;

public class TestPlatformHelper implements IPlatformHelper {
    @Override
    public List<IPlugin> getPlugins() {
        return List.of(new Plugin());
    }

    @Override
    public Path getConfiguration() {
        return null;
    }

    @Override
    public void renderBlockInGui(GuiGraphics guiGraphics, BlockRenderState renderState) {

    }
}
