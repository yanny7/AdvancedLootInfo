package com.yanny.ali.fabric.platform;

import com.yanny.ali.pip.BlockRenderState;
import com.yanny.ali.platform.services.IClientPlatformHelper;
import net.minecraft.client.gui.GuiGraphics;

public class FabricClientPlatformHelper implements IClientPlatformHelper {
    @Override
    public void renderBlockInGui(GuiGraphics guiGraphics, BlockRenderState renderState) {
        guiGraphics.guiRenderState.submitPicturesInPictureState(renderState);
    }
}
