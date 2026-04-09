package com.yanny.ali.fabric.platform;

import com.yanny.ali.pip.BlockRenderState;
import com.yanny.ali.platform.services.IClientPlatformHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class FabricClientPlatformHelper implements IClientPlatformHelper {
    @Override
    public void renderBlockInGui(GuiGraphicsExtractor guiGraphics, BlockRenderState renderState) {
        guiGraphics.guiRenderState.addPicturesInPictureState(renderState);
    }
}
