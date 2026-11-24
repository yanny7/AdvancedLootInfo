package com.yanny.aci.compatibility;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.yanny.aci.api.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public class GuiGraphics {
    private static final int FULL_BRIGHT = 15728880;
    private static final Deque<Rect> SCISSOR_STACK = new ArrayDeque<>();

    private final PoseStack pose;
    private final MultiBufferSource.BufferSource bufferSource;

    public GuiGraphics(PoseStack pose) {
        this.pose = pose;
        this.bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
    }

    public PoseStack pose() {
        return pose;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return bufferSource;
    }

    public void enableScissor(int left, int top, int right, int bottom) {
        Rect rect = new Rect(left, top, right - left, bottom - top);
        Rect parent = SCISSOR_STACK.peek();

        if (parent != null) {
            int x = Math.max(rect.x(), parent.x());
            int y = Math.max(rect.y(), parent.y());
            rect = new Rect(x, y, Math.max(0, Math.min(rect.right(), parent.right()) - x), Math.max(0, Math.min(rect.bottom(), parent.bottom()) - y));
        }

        SCISSOR_STACK.push(rect);
        applyScissor(rect);
    }

    public void disableScissor() {
        SCISSOR_STACK.pop();
        applyScissor(SCISSOR_STACK.peek());
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        GuiComponent.fill(pose, x1, y1, x2, y2, color);
    }

    public int drawString(Font font, @Nullable String text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, @Nullable String text, int x, int y, int color, boolean shadow) {
        if (text == null) {
            return 0;
        }

        int width = font.drawInBatch(text, x, y, color, shadow, pose.last().pose(), bufferSource, false, 0, FULL_BRIGHT, font.isBidirectional());
        bufferSource.endBatch();
        return width;
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        int width = font.drawInBatch(text, x, y, color, shadow, pose.last().pose(), bufferSource, false, 0, FULL_BRIGHT);
        bufferSource.endBatch();
        return width;
    }

    public int drawString(Font font, Component text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        return drawString(font, text.getVisualOrderText(), x, y, color, shadow);
    }

    public void blit(ResourceLocation atlas, int x, int y, int u, int v, int width, int height) {
        blit(atlas, x, x + width, y, y + height, 0, width, height, u, v, 256, 256);
    }

    public void blit(ResourceLocation atlas, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        blit(atlas, x, x + width, y, y + height, 0, uWidth, vHeight, u, v, textureWidth, textureHeight);
    }

    public void blitNineSliced(ResourceLocation atlas, int x, int y, int width, int height, int sliceSize, int uWidth, int vHeight, int u, int v) {
        blitNineSliced(atlas, x, y, width, height, sliceSize, sliceSize, sliceSize, sliceSize, uWidth, vHeight, u, v);
    }

    public void blitNineSliced(ResourceLocation atlas, int x, int y, int width, int height, int left, int top, int right, int bottom, int uWidth, int vHeight, int u, int v) {
        left = Math.min(left, width / 2);
        right = Math.min(right, width / 2);
        top = Math.min(top, height / 2);
        bottom = Math.min(bottom, height / 2);

        if (width == uWidth && height == vHeight) {
            blit(atlas, x, y, u, v, width, height);
        } else if (height == vHeight) {
            blit(atlas, x, y, u, v, left, height);
            blitRepeating(atlas, x + left, y, width - right - left, height, u + left, v, uWidth - right - left, vHeight);
            blit(atlas, x + width - right, y, u + uWidth - right, v, right, height);
        } else if (width == uWidth) {
            blit(atlas, x, y, u, v, width, top);
            blitRepeating(atlas, x, y + top, width, height - bottom - top, u, v + top, uWidth, vHeight - bottom - top);
            blit(atlas, x, y + height - bottom, u, v + vHeight - bottom, width, bottom);
        } else {
            blit(atlas, x, y, u, v, left, top);
            blitRepeating(atlas, x + left, y, width - right - left, top, u + left, v, uWidth - right - left, top);
            blit(atlas, x + width - right, y, u + uWidth - right, v, right, top);
            blit(atlas, x, y + height - bottom, u, v + vHeight - bottom, left, bottom);
            blitRepeating(atlas, x + left, y + height - bottom, width - right - left, bottom, u + left, v + vHeight - bottom, uWidth - right - left, bottom);
            blit(atlas, x + width - right, y + height - bottom, u + uWidth - right, v + vHeight - bottom, right, bottom);
            blitRepeating(atlas, x, y + top, left, height - bottom - top, u, v + top, left, vHeight - bottom - top);
            blitRepeating(atlas, x + left, y + top, width - right - left, height - bottom - top, u + left, v + top, uWidth - right - left, vHeight - bottom - top);
            blitRepeating(atlas, x + width - right, y + top, left, height - bottom - top, u + uWidth - right, v + top, right, vHeight - bottom - top);
        }
    }

    public void blitRepeating(ResourceLocation atlas, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight) {
        int posX = x;

        for (int sliceWidth : slices(width, uWidth)) {
            int offsetU = (uWidth - sliceWidth) / 2;
            int posY = y;

            for (int sliceHeight : slices(height, vHeight)) {
                int offsetV = (vHeight - sliceHeight) / 2;
                blit(atlas, posX, posY, u + offsetU, v + offsetV, sliceWidth, sliceHeight);
                posY += sliceHeight;
            }

            posX += sliceWidth;
        }
    }

    private void blit(ResourceLocation atlas, int x1, int x2, int y1, int y2, int blitOffset, int uWidth, int vHeight, float u, float v, int textureWidth, int textureHeight) {
        innerBlit(atlas, x1, x2, y1, y2, blitOffset, u / textureWidth, (u + uWidth) / textureWidth, v / textureHeight, (v + vHeight) / textureHeight);
    }

    private void innerBlit(ResourceLocation atlas, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV) {
        RenderSystem.setShaderTexture(0, atlas);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = pose.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x1, y1, blitOffset).uv(minU, minV).endVertex();
        builder.vertex(matrix, x1, y2, blitOffset).uv(minU, maxV).endVertex();
        builder.vertex(matrix, x2, y2, blitOffset).uv(maxU, maxV).endVertex();
        builder.vertex(matrix, x2, y1, blitOffset).uv(maxU, minV).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }

    private static int[] slices(int total, int max) {
        int count = Mth.positiveCeilDiv(total, max);
        int[] result = new int[count];
        int remainder = 0;

        for (int i = 0; i < count; i++) {
            result[i] = total / count;
            remainder += total % count;

            if (remainder >= count) {
                remainder -= count;
                result[i]++;
            }
        }

        return result;
    }

    private static void applyScissor(@Nullable Rect rect) {
        if (rect != null) {
            Window window = Minecraft.getInstance().getWindow();
            double scale = window.getGuiScale();
            double x = rect.x() * scale;
            double y = window.getHeight() - rect.bottom() * scale;
            RenderSystem.enableScissor((int) x, (int) y, Math.max(0, (int) (rect.width() * scale)), Math.max(0, (int) (rect.height() * scale)));
        } else {
            RenderSystem.disableScissor();
        }
    }
}
