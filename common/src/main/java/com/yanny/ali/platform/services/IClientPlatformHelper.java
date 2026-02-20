package com.yanny.ali.platform.services;

import com.yanny.ali.pip.BlockRenderState;
import net.minecraft.client.gui.GuiGraphics;

public interface IClientPlatformHelper {
    void renderBlockInGui(GuiGraphics guiGraphics, BlockRenderState renderState);
}
