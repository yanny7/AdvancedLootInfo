package com.yanny.ali.platform.services;

import com.yanny.ali.pip.BlockRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface IClientPlatformHelper {
        void renderBlockInGui(GuiGraphicsExtractor guiGraphics, BlockRenderState renderState);
}