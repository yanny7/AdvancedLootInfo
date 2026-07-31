package com.yanny.awi.fabric.platform;

import com.yanny.awi.pip.BlockRenderState;
import com.yanny.awi.platform.services.IClientPlatformHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class FabricClientPlatformHelper implements IClientPlatformHelper {
    @Override
    public void renderBlockInGui(GuiGraphicsExtractor guiGraphics, BlockRenderState renderState) {
        guiGraphics.guiRenderState.addPicturesInPictureState(renderState);
    }
}
