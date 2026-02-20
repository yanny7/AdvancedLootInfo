package com.yanny.ali.neoforge.platform;

import com.yanny.ali.pip.BlockRenderState;
import com.yanny.ali.platform.services.IClientPlatformHelper;
import net.minecraft.client.gui.GuiGraphics;

public class NeoForgeClientPlatformHelper implements IClientPlatformHelper {
    @Override
    public void renderBlockInGui(GuiGraphics guiGraphics, BlockRenderState renderState) {
        guiGraphics.submitPictureInPictureRenderState(renderState);
    }
}
