package com.yanny.awi.fabric.platform;

import com.yanny.awi.pip.BlockRenderState;
import com.yanny.awi.platform.services.IClientPlatformHelper;
import net.minecraft.client.gui.GuiGraphics;

public class FabricClientPlatformHelper implements IClientPlatformHelper {
    @Override
    public void renderBlockInGui(GuiGraphics guiGraphics, BlockRenderState renderState) {
        guiGraphics.guiRenderState.submitPicturesInPictureState(renderState);
    }
}
