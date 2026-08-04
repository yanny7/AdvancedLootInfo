package com.yanny.awi.neoforge.platform;

import com.yanny.awi.pip.BlockRenderState;
import com.yanny.awi.platform.services.IClientPlatformHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class NeoForgeClientPlatformHelper implements IClientPlatformHelper {
    @Override
    public void renderBlockInGui(GuiGraphicsExtractor guiGraphics, BlockRenderState renderState) {
        guiGraphics.submitPictureInPictureRenderState(renderState);
    }
}
