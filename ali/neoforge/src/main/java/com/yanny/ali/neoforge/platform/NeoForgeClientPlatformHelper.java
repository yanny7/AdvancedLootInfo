package com.yanny.ali.neoforge.platform;

import com.yanny.ali.pip.BlockRenderState;
import com.yanny.ali.platform.services.IClientPlatformHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class NeoForgeClientPlatformHelper implements IClientPlatformHelper {
    @Override
    public void renderBlockInGui(GuiGraphicsExtractor guiGraphics, BlockRenderState renderState) {
        guiGraphics.submitPictureInPictureRenderState(renderState);
    }
}