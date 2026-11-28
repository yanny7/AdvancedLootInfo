package com.yanny.aci.compatibility;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class ScaledText {
    private ScaledText() {}

    // A font pixel must cover whole screen pixels, or odd GUI scales draw strokes 1 and 2 pixels wide
    public static float halfScale() {
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();

        return (float) (Math.max(1, Math.floor(guiScale / 2)) / guiScale);
    }

    public static void drawSlotRange(GuiGraphics guiGraphics, Font font, Component text) {
        PoseStack stack = guiGraphics.pose();
        float scale = halfScale();

        stack.pushPose();
        stack.translate(17, 17, 200);
        stack.scale(scale, scale, 1);
        guiGraphics.drawString(font, text, -font.width(text), -8, 0xFFFFFF, false);
        stack.popPose();
    }
}
