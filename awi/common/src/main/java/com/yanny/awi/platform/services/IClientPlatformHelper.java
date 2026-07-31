package com.yanny.awi.platform.services;

import com.yanny.awi.pip.BlockRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface IClientPlatformHelper {
        void renderBlockInGui(GuiGraphicsExtractor guiGraphics, BlockRenderState renderState);
}
