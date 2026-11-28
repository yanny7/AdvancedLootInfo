package com.yanny.aci.compatibility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public final class ScaledText {
    private ScaledText() {}

    // A font pixel must cover whole screen pixels, or odd GUI scales draw strokes 1 and 2 pixels wide
    public static float halfScale() {
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();

        return (float) (Math.max(1, Math.floor(guiScale / 2)) / guiScale);
    }

    public static void drawSlotRange(GuiGraphicsExtractor guiGraphics, Font font, Component text) {
        Matrix3x2fStack stack = guiGraphics.pose();
        float scale = halfScale();

        stack.pushMatrix();
        stack.translate(17, 17);
        stack.scale(scale);
        guiGraphics.text(font, text, -font.width(text), -8, -1, false);
        stack.popMatrix();
    }
}
