package com.yanny.ali.platform.services;

import com.yanny.ali.manager.PluginHolder;
import com.yanny.ali.pip.BlockRenderState;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public interface IPlatformHelper {
        List<PluginHolder> getPlugins();

        void renderBlockInGui(GuiGraphics guiGraphics, BlockRenderState renderState);
}