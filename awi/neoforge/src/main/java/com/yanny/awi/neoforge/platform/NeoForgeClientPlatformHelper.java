package com.yanny.awi.neoforge.platform;

import com.yanny.awi.pip.BlockRenderState;
import com.yanny.awi.platform.services.IClientPlatformHelper;
import net.minecraft.client.gui.GuiGraphics;

public class NeoForgeClientPlatformHelper implements IClientPlatformHelper {
    @Override
    public void renderBlockInGui(GuiGraphics guiGraphics, BlockRenderState renderState) {
        guiGraphics.submitPictureInPictureRenderState(renderState);
    }
}
