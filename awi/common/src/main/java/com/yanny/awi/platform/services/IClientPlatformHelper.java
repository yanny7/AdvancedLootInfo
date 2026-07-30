package com.yanny.awi.platform.services;

import com.yanny.awi.pip.BlockRenderState;
import net.minecraft.client.gui.GuiGraphics;

public interface IClientPlatformHelper {
        void renderBlockInGui(GuiGraphics guiGraphics, BlockRenderState renderState);
}
